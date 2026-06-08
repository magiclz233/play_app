package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.service.WxPayService;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.OrderCreateDTO;
import com.playapp.entity.CompanionProfile;
import com.playapp.entity.CompanionSkill;
import com.playapp.entity.Order;
import com.playapp.entity.OrderStatusLog;
import com.playapp.entity.ServiceRecord;
import com.playapp.entity.User;
import com.playapp.mapper.CompanionSkillMapper;
import com.playapp.mapper.OrderMapper;
import com.playapp.mapper.OrderStatusLogMapper;
import com.playapp.mapper.ServiceRecordMapper;
import com.playapp.service.CompanionProfileService;
import com.playapp.service.CompanionWalletService;
import com.playapp.service.OrderService;
import com.playapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CompanionProfileService companionProfileService;
    private final UserService userService;
    private final CompanionWalletService companionWalletService;
    private final CompanionSkillMapper companionSkillMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final ServiceRecordMapper serviceRecordMapper;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WxPayService wxPayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO dto) {
        // 1. 校验陪玩状态
        CompanionProfile companion = companionProfileService.getById(dto.getCompanionId());
        if (companion == null || companion.getAuditStatus() != 1) {
            throw new BusinessException(ErrorCode.COMPANION_NOT_FOUND, "助教不存在或已下线");
        }
        if (companion.getWorkStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "助教当前状态无法接单");
        }

        // 不能自己预约自己
        if (userId.equals(dto.getCompanionId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能预约自己");
        }

        // 2. 查询真实价格
        BigDecimal pricePerHour = new BigDecimal("100.00"); // 默认兜底
        Integer categoryId = dto.getServiceId();
        CompanionSkill skill = companionSkillMapper.selectOne(
                new LambdaQueryWrapper<CompanionSkill>()
                        .eq(CompanionSkill::getCompanionId, dto.getCompanionId())
                        .eq(CompanionSkill::getCategoryId, categoryId)
                        .eq(CompanionSkill::getStatus, 1)
                        .last("limit 1"));
        if (skill != null && skill.getPricePerHour() != null) {
            pricePerHour = skill.getPricePerHour();
        }

        BigDecimal totalAmount = pricePerHour.multiply(BigDecimal.valueOf(dto.getHours()));

        // 3. 生成内部订单号
        String orderNo = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));

        // 4. 创建订单记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCompanionId(dto.getCompanionId());
        order.setCategoryId(categoryId);
        order.setPricePerHour(pricePerHour);
        order.setStatus(Order.STATUS_PENDING_PAY);
        order.setTotalAmount(totalAmount);
        order.setCompanionAmount(totalAmount);
        order.setPlatformFee(BigDecimal.ZERO);
        order.setRefundAmount(BigDecimal.ZERO);
        if (dto.getAppointmentTime() != null) {
            order.setReserveDate(dto.getAppointmentTime().toLocalDate());
            order.setReserveTimeStart(dto.getAppointmentTime().toLocalTime());
            order.setReserveTimeEnd(dto.getAppointmentTime().plusHours(dto.getHours()).toLocalTime());
        }
        order.setHours(BigDecimal.valueOf(dto.getHours()));
        order.setAddress(dto.getAddress());
        order.setAddressDetail(dto.getAddressDetail());
        order.setCustomerWechat(dto.getCustomerWechat());
        order.setCustomerRemark(dto.getRemark());

        this.save(order);
        addStatusLog(order, null, Order.STATUS_PENDING_PAY, userId, 1, "用户创建订单");

        log.info("订单创建成功, orderNo: {}, userId: {}, companionId: {}", orderNo, userId, dto.getCompanionId());

        return orderNo;
    }

    @Override
    public Object getWxPrepayInfo(Long userId, String orderNo) {
        // 1. 获取订单
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        if (order.getStatus() != Order.STATUS_PENDING_PAY) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "订单状态不正确，无法支付");
        }

        // 2. 获取用户 OpenID
        User user = userService.getById(userId);
        if (user == null || user.getOpenid() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户信息异常");
        }

        // 3. Mock 支付（如果没有真实商户号配置）
        if (wxPayService == null) {
            log.warn("WxPayService 未注入，采用 Mock 支付流程");
            mockPaySuccess(orderNo);

            return java.util.Map.of(
                    "appId", "wx_mock_appid",
                    "timeStamp", String.valueOf(System.currentTimeMillis() / 1000),
                    "nonceStr", UUID.randomUUID().toString().replace("-", ""),
                    "package", "prepay_id=wx_mock_prepay_id",
                    "signType", "RSA",
                    "paySign", "mock_sign",
                    "mock", true
            );
        }

        // 4. 真实微信支付逻辑
        try {
            WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
            request.setBody("同城伴玩-服务预约");
            request.setOutTradeNo(orderNo);
            request.setTotalFee(order.getTotalAmount().multiply(new BigDecimal(100)).intValue());
            request.setSpbillCreateIp("127.0.0.1");
            request.setNotifyUrl("https://yourdomain.com/api/wx/pay/notify");
            request.setTradeType("JSAPI");
            request.setOpenid(user.getOpenid());

            return wxPayService.createOrder(request);

        } catch (Exception e) {
            log.error("微信支付下单失败", e);
            throw new BusinessException("生成支付参数失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWxPayNotify(String xmlData) {
        if (wxPayService == null) return "FAIL";

        try {
            WxPayOrderNotifyResult notifyResult = wxPayService.parseOrderNotifyResult(xmlData);
            String orderNo = notifyResult.getOutTradeNo();
            String transactionId = notifyResult.getTransactionId();

            Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
            if (order != null && order.getStatus() == Order.STATUS_PENDING_PAY) {
                int fromStatus = order.getStatus();
                order.setStatus(Order.STATUS_PAID);
                order.setTransactionId(transactionId);
                order.setPayTime(LocalDateTime.now());
                this.updateById(order);
                addStatusLog(order, fromStatus, Order.STATUS_PAID, 0L, 4, "微信支付回调成功");
                log.info("订单支付成功回调处理完成: {}", orderNo);
            }

            return WxPayNotifyResponse.success("OK");
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

    private void mockPaySuccess(String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order != null && order.getStatus() == Order.STATUS_PENDING_PAY) {
            int fromStatus = order.getStatus();
            order.setStatus(Order.STATUS_PAID);
            order.setTransactionId("mock_trans_" + System.currentTimeMillis());
            order.setPayTime(LocalDateTime.now());
            this.updateById(order);
            addStatusLog(order, fromStatus, Order.STATUS_PAID, 0L, 4, "Mock 支付成功");
            log.info("Mock: 订单直接更新为支付成功: {}", orderNo);
        }
    }

    private static class WxPayNotifyResponse {
        public static String success(String msg) {
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
        }

        public static String fail(String msg) {
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
        }
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> getMyOrders(Long userId, Integer status, Integer current, Integer size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Order getOrderDetail(Long userId, String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || (!order.getUserId().equals(userId) && !order.getCompanionId().equals(userId))) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权访问");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmService(Long userId, String orderNo) {
        Order order = this.getOrderDetail(userId, orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有下单用户才能确认服务完成");
        }
        if (order.getStatus() != Order.STATUS_FINISH_REQUESTED && order.getStatus() != Order.STATUS_USER_CONFIRMED) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前订单状态无法确认完工");
        }

        if (order.getStatus() == Order.STATUS_USER_CONFIRMED) {
            return;
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_USER_CONFIRMED);
        order.setFinishTime(LocalDateTime.now());
        this.updateById(order);
        addStatusLog(order, fromStatus, Order.STATUS_USER_CONFIRMED, userId, 1, "用户确认完工，等待平台结算");

        log.info("订单已由用户确认完工，等待平台结算: {}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long companionId, String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }
        if (order.getStatus() != Order.STATUS_PAID) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "当前订单状态无法接单");
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_GROUP_CREATED);
        order.setWechatGroupStatus(1);
        this.updateById(order);
        ensureServiceRecord(order);
        addStatusLog(order, fromStatus, Order.STATUS_GROUP_CREATED, companionId, 2, "助教接单");
        log.info("助教 {} 已接单: {}", companionId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(Long companionId, String orderNo, String reason) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }
        if (order.getStatus() != Order.STATUS_PAID) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "当前订单状态无法拒单");
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_CANCEL_REQUESTED);
        order.setCancelReason(reason);
        order.setCancelType(2); // 2-助教拒单
        this.updateById(order);
        addStatusLog(order, fromStatus, Order.STATUS_CANCEL_REQUESTED, companionId, 2, reason);
        log.info("助教 {} 已拒单: {}, 原因: {}", companionId, orderNo, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo, String reason) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }
        int status = order.getStatus();
        if (status != Order.STATUS_PENDING_PAY && status != Order.STATUS_PAID) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "当前订单状态无法取消");
        }

        // 已付款的进入退款流程，未付款的直接关闭
        int fromStatus = order.getStatus();
        if (status == Order.STATUS_PAID) {
            order.setStatus(Order.STATUS_CANCEL_REQUESTED);
            order.setRefundAmount(order.getTotalAmount());
        } else {
            order.setStatus(Order.STATUS_CLOSED);
        }
        order.setCancelReason(reason);
        order.setCancelType(1); // 1-用户取消
        this.updateById(order);
        addStatusLog(order, fromStatus, order.getStatus(), userId, 1, reason);
        log.info("用户 {} 取消订单: {}, 原因: {}", userId, orderNo, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, String orderNo, String reason) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }

        int status = order.getStatus();
        if (status != Order.STATUS_PAID
                && status != Order.STATUS_GROUP_CREATED
                && status != Order.STATUS_CONFIRMED
                && status != Order.STATUS_IN_SERVICE
                && status != Order.STATUS_FINISH_REQUESTED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "当前订单状态无法申请退款");
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_CANCEL_REQUESTED);
        order.setRefundAmount(order.getTotalAmount());
        order.setCancelReason(reason);
        order.setCancelType(1);
        this.updateById(order);
        addStatusLog(order, fromStatus, Order.STATUS_CANCEL_REQUESTED, userId, 1, reason);
        log.info("用户 {} 申请退款: {}, 原因: {}", userId, orderNo, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminMarkGroupCreated(Long adminId, String orderNo, String remark) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != Order.STATUS_PAID) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "只有已付款订单才能标记拉群完成");
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_GROUP_CREATED);
        order.setWechatGroupStatus(1);
        this.updateById(order);
        ensureServiceRecord(order);
        addStatusLog(order, fromStatus, Order.STATUS_GROUP_CREATED, adminId, 3, defaultText(remark, "客服已完成三方群对接"));
        log.info("管理员 {} 标记订单已拉群: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminStartService(Long adminId, String orderNo, String actualAddress, String remark) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != Order.STATUS_GROUP_CREATED && order.getStatus() != Order.STATUS_CONFIRMED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "只有已拉群订单才能标记服务开始");
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_IN_SERVICE);
        this.updateById(order);

        ServiceRecord record = ensureServiceRecord(order);
        if (record.getActualStartTime() == null) {
            record.setActualStartTime(LocalDateTime.now());
        }
        if (actualAddress != null && !actualAddress.isBlank()) {
            record.setActualAddress(actualAddress);
        }
        serviceRecordMapper.updateById(record);

        addStatusLog(order, fromStatus, Order.STATUS_IN_SERVICE, adminId, 3, defaultText(remark, "服务已开始"));
        log.info("管理员 {} 标记订单服务开始: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminConfirmFinish(Long adminId, String orderNo, String finishRemark, Integer finishType) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != Order.STATUS_GROUP_CREATED
                && order.getStatus() != Order.STATUS_CONFIRMED
                && order.getStatus() != Order.STATUS_IN_SERVICE
                && order.getStatus() != Order.STATUS_FINISH_REQUESTED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "当前订单状态无法核销完工");
        }

        LocalDateTime now = LocalDateTime.now();
        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_USER_CONFIRMED);
        order.setFinishTime(now);
        this.updateById(order);

        ServiceRecord record = ensureServiceRecord(order);
        if (record.getActualStartTime() == null) {
            record.setActualStartTime(now);
        }
        record.setActualEndTime(now);
        record.setActualDuration((int) Duration.between(record.getActualStartTime(), now).toMinutes());
        record.setFinishRemark(finishRemark);
        record.setFinishType(finishType == null ? 1 : finishType);
        serviceRecordMapper.updateById(record);

        addStatusLog(order, fromStatus, Order.STATUS_USER_CONFIRMED, adminId, 3, defaultText(finishRemark, "平台核销完工"));
        log.info("管理员 {} 核销订单完工: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminSettleOrder(Long adminId, String orderNo, String remark) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() != Order.STATUS_USER_CONFIRMED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "只有已确认完工订单才能结算");
        }

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_SETTLED);
        this.updateById(order);
        companionWalletService.addBalance(order.getCompanionId(), order.getTotalAmount(), order.getOrderId(), order.getOrderNo());
        addStatusLog(order, fromStatus, Order.STATUS_SETTLED, adminId, 3, defaultText(remark, "平台结算放款"));
        log.info("管理员 {} 结算订单并放款: {}", adminId, orderNo);
    }

    private Order getOrderByNo(String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private ServiceRecord ensureServiceRecord(Order order) {
        ServiceRecord record = serviceRecordMapper.selectOne(
                new LambdaQueryWrapper<ServiceRecord>().eq(ServiceRecord::getOrderId, order.getOrderId()));
        if (record != null) {
            return record;
        }

        record = new ServiceRecord();
        record.setOrderId(order.getOrderId());
        record.setCompanionId(order.getCompanionId());
        serviceRecordMapper.insert(record);
        return record;
    }

    private void addStatusLog(Order order, Integer fromStatus, Integer toStatus, Long operatorId, Integer operatorRole, String reason) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getOrderId());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId == null ? 0L : operatorId);
        log.setOperatorRole(operatorRole);
        log.setChangeReason(reason);
        orderStatusLogMapper.insert(log);
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }
}
