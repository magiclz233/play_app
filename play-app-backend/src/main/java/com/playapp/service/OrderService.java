package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.OrderCreateDTO;
import com.playapp.entity.Order;

public interface OrderService extends IService<Order> {

    /**
     * 创建预约订单
     * @param userId 下单用户ID
     * @param dto 订单参数
     * @return 订单号
     */
    String createOrder(Long userId, OrderCreateDTO dto);

    /**
     * 获取微信支付统一下单参数
     * @param userId 用户ID
     * @param orderNo 内部订单号
     * @return 微信支付所需参数 (JSON)
     */
    Object getWxPrepayInfo(Long userId, String orderNo);
    
    /**
     * 处理微信支付异步回调
     * @param xmlData 微信返回的xml/json结果
     * @return 处理结果响应给微信
     */
    String handleWxPayNotify(String xmlData);

    /**
     * 获取我的订单列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> getMyOrders(Long userId, Integer status, Integer current, Integer size);

    /**
     * 获取订单详情
     */
    Order getOrderDetail(Long userId, String orderNo);

    /**
     * 确认服务完成
     */
    void confirmService(Long userId, String orderNo);

    /**
     * 助教接单
     */
    void acceptOrder(Long companionId, String orderNo);

    /**
     * 助教拒单
     */
    void rejectOrder(Long companionId, String orderNo, String reason);

    /**
     * 客户取消订单
     */
    void cancelOrder(Long userId, String orderNo, String reason);
}
