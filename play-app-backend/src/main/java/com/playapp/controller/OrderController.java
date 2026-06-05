package com.playapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.dto.OrderCreateDTO;
import com.playapp.entity.Order;
import com.playapp.service.OrderService;
import com.playapp.service.ReviewService;
import com.playapp.dto.ReviewDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ReviewService reviewService;

    /**
     * 创建预约订单
     */
    @PostMapping
    public Result<String> createOrder(@AuthenticationPrincipal Long userId, @Valid @RequestBody OrderCreateDTO dto) {
        String orderNo = orderService.createOrder(userId, dto);
        return Result.success("订单创建成功", orderNo);
    }

    /**
     * 获取微信支付参数
     */
    @PostMapping("/{orderNo}/prepay")
    public Result<Object> getPrepayInfo(@AuthenticationPrincipal Long userId, @PathVariable String orderNo) {
        Object payInfo = orderService.getWxPrepayInfo(userId, orderNo);
        return Result.success("获取支付参数成功", payInfo);
    }

    /**
     * 获取我的订单列表
     */
    @GetMapping
    public Result<Page<Order>> getMyOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(orderService.getMyOrders(userId, status, current, size));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@AuthenticationPrincipal Long userId, @PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(userId, orderNo));
    }

    /**
     * 确认服务完成
     */
    @PutMapping("/{orderNo}/confirm")
    public Result<?> confirmService(@AuthenticationPrincipal Long userId, @PathVariable String orderNo) {
        orderService.confirmService(userId, orderNo);
        return Result.success("已确认服务完成");
    }

    /**
     * 提交订单评价
     */
    @PostMapping("/{orderNo}/review")
    public Result<?> submitReview(@AuthenticationPrincipal Long userId, @PathVariable String orderNo, @Valid @RequestBody ReviewDTO dto) {
        reviewService.submitReview(userId, orderNo, dto);
        return Result.success("评价提交成功");
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<?> cancelOrder(@AuthenticationPrincipal Long userId, @PathVariable String orderNo, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        orderService.cancelOrder(userId, orderNo, reason);
        return Result.success("订单已取消");
    }
}
