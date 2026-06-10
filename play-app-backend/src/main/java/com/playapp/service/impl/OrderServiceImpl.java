package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.service.WxPayService;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.CompanionFinishDTO;
import com.playapp.dto.DisputeCreateDTO;
import com.playapp.dto.DisputeResolveDTO;
import com.playapp.dto.OrderCreateDTO;
import com.playapp.entity.CompanionProfile;
import com.playapp.entity.CompanionSkill;
import com.playapp.entity.Order;
import com.playapp.entity.OrderStatusLog;
import com.playapp.entity.PaymentRecord;
import com.playapp.entity.ServiceRecord;
import com.playapp.entity.User;
import com.playapp.mapper.CompanionSkillMapper;
import com.playapp.mapper.OrderMapper;
import com.playapp.mapper.OrderStatusLogMapper;
import com.playapp.mapper.PaymentRecordMapper;
import com.playapp.mapper.ServiceRecordMapper;
import com.playapp.service.AdminAuditLogService;
import com.playapp.service.CompanionProfileService;
import com.playapp.service.CompanionWalletService;
import com.playapp.service.OrderService;
import com.playapp.service.UserService;
import com.playapp.vo.OrderCreateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final PaymentRecordMapper paymentRecordMapper;
    private final AdminAuditLogService auditLogService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WxPayService wxPayService;

    @Value("${app.platform.fee-rate:0.05}")
    private BigDecimal feeRate;

    @Value("${app.platform.fee-min:5.00}")
    private BigDecimal feeMin;

    @Value("${app.platform.fee-max:50.00}")
    private BigDecimal feeMax;

    /**
     * 订单状态转移映射表
     * key=当前状态, value=允许转移到的目标状态集合
     */
    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(10, Set.of(20, 100, 250)),          // 待付款 → 已付款/取消/超时关闭
            Map.entry(20, Set.of(30, 100, 110, 200)),     // 已付款 → 客服拉群/取消/退款中/纠纷
            Map.entry(30, Set.of(40, 50, 70, 100, 200)),  // 已拉群 → 确认/服务中(admin)/确认完工(admin)/取消/纠纷
            Map.entry(40, Set.of(50, 70, 100, 200)),      // 已确认 → 服务中/确认完工(admin)/取消/纠纷
            Map.entry(50, Set.of(60, 70, 100, 200)),      // 服务中 → 完工/确认完工(admin)/取消/纠纷
            Map.entry(60, Set.of(70, 100, 200)),           // 待确认完工 → 用户确认/取消/纠纷
            Map.entry(70, Set.of(80, 200)),                // 已确认完工 → 平台放款/纠纷
            Map.entry(80, Set.of(200)),                    // 已完成 → 纠纷
            Map.entry(100, Set.of(110)),                   // 取消申请 → 退款处理中
            Map.entry(110, Set.of(120, 130)),              // 退款处理中 → 全额退款/部分退款
            Map.entry(200, Set.of(210))                    // 纠纷申诉 → 纠纷处理完成
            // 120(全额退款), 130(部分退款), 210(纠纷完成), 250(已关闭) 为终态, 不允许再转移
    );

    /**
     * 校验订单状态转移是否合法
     */
    private void assertValidTransition(Integer fromStatus, Integer toStatus) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowed == null || !allowed.contains(toStatus)) {
            throw BusinessException.orderStatusIllegal(fromStatus, toStatus);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Long userId, OrderCreateDTO dto) {
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

        // 3. 计算平台费用（费率 * 总金额，最低 feeMin 元，最高 feeMax 元封顶）
        BigDecimal rawFee = totalAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformFee = rawFee.max(feeMin).min(feeMax);
        BigDecimal companionAmount = totalAmount.subtract(platformFee);

        // 4. 生成内部订单号
        String orderNo = "PO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));

        // 5. 创建订单记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCompanionId(dto.getCompanionId());
        order.setCategoryId(categoryId);
        order.setPricePerHour(pricePerHour);
        order.setStatus(Order.STATUS_PENDING_PAY);
        order.setTotalAmount(totalAmount);
        order.setCompanionAmount(companionAmount);
        order.setPlatformFee(platformFee);
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

        log.info("订单创建成功, orderNo: {}, userId: {}, companionId: {}, totalAmount: {}, platformFee: {}, companionAmount: {}",
                orderNo, userId, dto.getCompanionId(), totalAmount, platformFee, companionAmount);

        return new OrderCreateVO(orderNo, totalAmount, platformFee, companionAmount);
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
            if (order != null) {
                markPaid(order, transactionId, xmlData, "微信支付回调成功");
            }

            return WxPayNotifyResponse.success("OK");
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return WxPayNotifyResponse.fail(e.getMessage());
        }
    }

    private void mockPaySuccess(String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order != null) {
            markPaid(order, "mock_trans_" + orderNo, null, "Mock 支付成功");
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
        // 幂等：已确认则跳过
        if (order.getStatus() == Order.STATUS_USER_CONFIRMED) {
            return;
        }
        // 状态转移校验
        assertValidTransition(order.getStatus(), Order.STATUS_USER_CONFIRMED);

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
        assertValidTransition(order.getStatus(), Order.STATUS_GROUP_CREATED);

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
        assertValidTransition(order.getStatus(), Order.STATUS_CANCEL_REQUESTED);

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
    public void companionRequestFinish(Long companionId, String orderNo, CompanionFinishDTO dto) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getCompanionId().equals(companionId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }
        assertValidTransition(order.getStatus(), Order.STATUS_FINISH_REQUESTED);

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_FINISH_REQUESTED);
        this.updateById(order);

        ServiceRecord record = ensureServiceRecord(order);
        record.setFinishRemark(dto.getFinishRemark());
        record.setFinishType(dto.getFinishType() != null ? dto.getFinishType() : 1);
        if (dto.getActualDuration() != null && dto.getActualDuration() > 0) {
            record.setActualDuration(dto.getActualDuration());
            record.setActualEndTime(LocalDateTime.now());
        }
        serviceRecordMapper.updateById(record);

        addStatusLog(order, fromStatus, Order.STATUS_FINISH_REQUESTED, companionId, 2,
                "陪玩发起完工: " + dto.getFinishRemark());
        log.info("陪玩 {} 发起完工申请: {}, 备注: {}", companionId, orderNo, dto.getFinishRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo, String reason) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }
        int status = order.getStatus();

        // 已付款的进入退款流程，未付款的直接关闭
        int targetStatus;
        if (status == Order.STATUS_PAID) {
            targetStatus = Order.STATUS_CANCEL_REQUESTED;
            order.setRefundAmount(order.getTotalAmount());
        } else if (status == Order.STATUS_PENDING_PAY) {
            targetStatus = Order.STATUS_CLOSED;
        } else {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL, "当前订单状态无法取消");
        }
        assertValidTransition(status, targetStatus);

        int fromStatus = order.getStatus();
        order.setStatus(targetStatus);
        order.setCancelReason(reason);
        order.setCancelType(1); // 1-用户取消
        this.updateById(order);
        addStatusLog(order, fromStatus, targetStatus, userId, 1, reason);
        log.info("用户 {} 取消订单: {}, 原因: {}", userId, orderNo, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, String orderNo, String reason) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权操作");
        }

        assertValidTransition(order.getStatus(), Order.STATUS_CANCEL_REQUESTED);

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
        assertValidTransition(order.getStatus(), Order.STATUS_GROUP_CREATED);

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_GROUP_CREATED);
        order.setWechatGroupStatus(1);
        this.updateById(order);
        ensureServiceRecord(order);
        addStatusLog(order, fromStatus, Order.STATUS_GROUP_CREATED, adminId, 3, defaultText(remark, "客服已完成三方群对接"));
        auditLogService.record(adminId, "ORDER_GROUP_CREATED", "order", orderNo, remark);
        log.info("管理员 {} 标记订单已拉群: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminStartService(Long adminId, String orderNo, String actualAddress, String remark) {
        Order order = getOrderByNo(orderNo);
        assertValidTransition(order.getStatus(), Order.STATUS_IN_SERVICE);

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
        auditLogService.record(adminId, "ORDER_START_SERVICE", "order", orderNo, remark);
        log.info("管理员 {} 标记订单服务开始: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminConfirmFinish(Long adminId, String orderNo, String finishRemark, Integer finishType) {
        Order order = getOrderByNo(orderNo);
        assertValidTransition(order.getStatus(), Order.STATUS_USER_CONFIRMED);

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
        auditLogService.record(adminId, "ORDER_FINISH", "order", orderNo, finishRemark);
        log.info("管理员 {} 核销订单完工: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminSettleOrder(Long adminId, String orderNo, String remark) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() == Order.STATUS_SETTLED) {
            return; // 幂等
        }
        assertValidTransition(order.getStatus(), Order.STATUS_SETTLED);

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_SETTLED);
        this.updateById(order);
        companionWalletService.addBalance(order.getCompanionId(), order.getCompanionAmount(), order.getOrderId(), order.getOrderNo());
        recordPayment(order, "SETTLE-" + order.getOrderNo(), null, 4, order.getCompanionAmount(), 1, remark);
        addStatusLog(order, fromStatus, Order.STATUS_SETTLED, adminId, 3, defaultText(remark, "平台结算放款"));
        auditLogService.record(adminId, "ORDER_SETTLE", "order", orderNo, remark);
        log.info("管理员 {} 结算订单并放款: {}", adminId, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminApproveRefund(Long adminId, String orderNo, String remark) {
        Order order = getOrderByNo(orderNo);
        if (order.getStatus() == Order.STATUS_FULL_REFUNDED || order.getStatus() == Order.STATUS_PARTIAL_REFUNDED) {
            return; // 幂等
        }

        // 确定退款金额和退款类型
        BigDecimal refundAmount = order.getRefundAmount() != null && order.getRefundAmount().compareTo(BigDecimal.ZERO) > 0
                ? order.getRefundAmount()
                : order.getTotalAmount();

        boolean isFullRefund = refundAmount.compareTo(order.getTotalAmount()) >= 0;
        int targetStatus = isFullRefund ? Order.STATUS_FULL_REFUNDED : Order.STATUS_PARTIAL_REFUNDED;
        assertValidTransition(order.getStatus(), targetStatus);

        int fromStatus = order.getStatus();
        order.setStatus(targetStatus);
        order.setRefundAmount(refundAmount);
        this.updateById(order);

        int paymentType = isFullRefund ? 2 : 3; // 2-全额退款 3-部分退款
        recordPayment(order, "REFUND-" + order.getOrderNo(), order.getTransactionId(), paymentType, refundAmount, 1, remark);
        addStatusLog(order, fromStatus, targetStatus, adminId, 3,
                defaultText(remark, isFullRefund ? "平台确认全额退款" : "平台确认部分退款"));
        auditLogService.record(adminId, "ORDER_REFUND", "order", orderNo, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCloseExpiredOrders() {
        int timeoutMinutes = 15; // 默认15分钟，可从配置读取
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

        List<Order> expiredOrders = this.list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, Order.STATUS_PENDING_PAY)
                        .lt(Order::getCreateTime, expireTime));

        int closedCount = 0;
        for (Order order : expiredOrders) {
            try {
                assertValidTransition(order.getStatus(), Order.STATUS_CLOSED);
                int fromStatus = order.getStatus();
                order.setStatus(Order.STATUS_CLOSED);
                order.setCancelReason("超时未支付自动关闭");
                order.setCancelType(4); // 4-系统自动关闭
                this.updateById(order);
                addStatusLog(order, fromStatus, Order.STATUS_CLOSED, 0L, 4, "超时未支付自动关闭");
                closedCount++;
            } catch (Exception e) {
                log.error("自动关闭订单失败: orderId={}, orderNo={}", order.getOrderId(), order.getOrderNo(), e);
            }
        }

        if (closedCount > 0) {
            log.info("定时任务：自动关闭超时未支付订单 {} 笔", closedCount);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDispute(Long userId, String role, DisputeCreateDTO dto) {
        Order order = getOrderByNo(dto.getOrderNo());
        // 验证权限：用户或陪玩
        if (!order.getUserId().equals(userId) && !order.getCompanionId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此订单");
        }
        assertValidTransition(order.getStatus(), Order.STATUS_DISPUTE);

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_DISPUTE);
        // 将纠纷信息存入 extra JSONB
        java.util.Map<String, Object> extra = order.getExtra() != null ? order.getExtra() : new java.util.HashMap<>();
        java.util.Map<String, Object> dispute = new java.util.LinkedHashMap<>();
        dispute.put("initiator", role);
        dispute.put("initiatorId", userId);
        dispute.put("reasonType", dto.getReasonType());
        dispute.put("description", dto.getDescription());
        dispute.put("evidenceUrls", dto.getEvidenceUrls());
        dispute.put("createTime", LocalDateTime.now().toString());
        extra.put("dispute", dispute);
        order.setExtra(extra);
        this.updateById(order);
        addStatusLog(order, fromStatus, Order.STATUS_DISPUTE, userId, role.equals("companion") ? 2 : 1,
                "发起纠纷申诉: " + dto.getDescription());
        log.info("订单 {} 纠纷申诉已提交, 发起方: {}", dto.getOrderNo(), role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveDispute(Long adminId, DisputeResolveDTO dto) {
        Order order = getOrderByNo(dto.getOrderNo());
        assertValidTransition(order.getStatus(), Order.STATUS_DISPUTE_RESOLVED);

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_DISPUTE_RESOLVED);
        // 更新 extra 中的纠纷解决信息
        java.util.Map<String, Object> extra = order.getExtra() != null ? order.getExtra() : new java.util.HashMap<>();
        java.util.Map<String, Object> dispute = extra.containsKey("dispute")
                ? (java.util.Map<String, Object>) extra.get("dispute") : new java.util.LinkedHashMap<>();
        dispute.put("resolution", dto.getResolution());
        dispute.put("resolvedBy", adminId);
        dispute.put("resolvedAt", LocalDateTime.now().toString());
        dispute.put("remark", dto.getRemark());
        extra.put("dispute", dispute);
        order.setExtra(extra);
        this.updateById(order);
        addStatusLog(order, fromStatus, Order.STATUS_DISPUTE_RESOLVED, adminId, 3,
                "管理员处理纠纷: " + dto.getResolution());
        auditLogService.record(adminId, "DISPUTE_RESOLVE", "order", dto.getOrderNo(), dto.getRemark());

        // 如果需要退款
        if ("refund_full".equals(dto.getResolution()) || "refund_partial".equals(dto.getResolution())) {
            adminApproveRefund(adminId, dto.getOrderNo(), dto.getRemark());
        }
        log.info("管理员 {} 处理纠纷: orderNo={}, resolution={}", adminId, dto.getOrderNo(), dto.getResolution());
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

    private void markPaid(Order order, String transactionId, String notifyData, String reason) {
        // 支付流水去重
        if (transactionId != null && paymentRecordMapper.selectCount(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getWxTransactionId, transactionId)) > 0) {
            return;
        }
        // 幂等：已支付则跳过
        if (order.getStatus() == Order.STATUS_PAID) {
            return;
        }
        // 状态转移校验
        assertValidTransition(order.getStatus(), Order.STATUS_PAID);

        int fromStatus = order.getStatus();
        order.setStatus(Order.STATUS_PAID);
        order.setTransactionId(transactionId);
        order.setPayTime(LocalDateTime.now());
        this.updateById(order);
        addStatusLog(order, fromStatus, Order.STATUS_PAID, 0L, 4, reason);
        recordPayment(order, "PAY-" + order.getOrderNo(), transactionId, 1, order.getTotalAmount(), 1, notifyData);
        log.info("订单支付成功处理完成: {}", order.getOrderNo());
    }

    private void recordPayment(Order order, String paymentNo, String wxTransactionId, Integer paymentType,
                               BigDecimal amount, Integer status, String notifyData) {
        Long exists = paymentRecordMapper.selectCount(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getPaymentNo, paymentNo));
        if (exists != null && exists > 0) {
            return;
        }
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(order.getOrderId());
        record.setPaymentNo(paymentNo);
        record.setWxTransactionId(wxTransactionId);
        record.setPaymentType(paymentType);
        record.setAmount(amount);
        record.setStatus(status);
        record.setPaidTime(LocalDateTime.now());
        record.setNotifyData(notifyData);
        paymentRecordMapper.insert(record);
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }
}
