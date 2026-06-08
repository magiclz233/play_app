package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.dto.AdminOrderActionDTO;
import com.playapp.entity.Order;
import com.playapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 获取全平台订单列表
     */
    @GetMapping
    public Result<Page<Order>> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        return Result.success(orderService.page(page, wrapper));
    }

    @PutMapping("/{orderNo}/group-created")
    public Result<?> markGroupCreated(
            @AuthenticationPrincipal Long adminId,
            @PathVariable String orderNo,
            @RequestBody(required = false) AdminOrderActionDTO dto) {
        orderService.adminMarkGroupCreated(adminId, orderNo, dto == null ? null : dto.getRemark());
        return Result.success("已标记客服拉群完成");
    }

    @PutMapping("/{orderNo}/start")
    public Result<?> startService(
            @AuthenticationPrincipal Long adminId,
            @PathVariable String orderNo,
            @RequestBody(required = false) AdminOrderActionDTO dto) {
        orderService.adminStartService(
                adminId,
                orderNo,
                dto == null ? null : dto.getActualAddress(),
                dto == null ? null : dto.getRemark());
        return Result.success("已标记服务开始");
    }

    @PutMapping("/{orderNo}/finish")
    public Result<?> confirmFinish(
            @AuthenticationPrincipal Long adminId,
            @PathVariable String orderNo,
            @RequestBody(required = false) AdminOrderActionDTO dto) {
        orderService.adminConfirmFinish(
                adminId,
                orderNo,
                dto == null ? null : dto.getFinishRemark(),
                dto == null ? null : dto.getFinishType());
        return Result.success("已核销完工");
    }

    @PutMapping("/{orderNo}/settle")
    public Result<?> settleOrder(
            @AuthenticationPrincipal Long adminId,
            @PathVariable String orderNo,
            @RequestBody(required = false) AdminOrderActionDTO dto) {
        orderService.adminSettleOrder(adminId, orderNo, dto == null ? null : dto.getRemark());
        return Result.success("已结算放款");
    }
}
