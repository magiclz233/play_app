package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.dto.WithdrawDTO;
import com.playapp.entity.CompanionWallet;
import com.playapp.entity.Order;
import com.playapp.entity.WalletTransaction;
import com.playapp.service.CompanionWalletService;
import com.playapp.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companion")
@RequiredArgsConstructor
public class CompanionDashboardController {

    private final CompanionWalletService companionWalletService;
    private final OrderService orderService;

    /**
     * 获取助教工作台统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats(@AuthenticationPrincipal Long userId) {
        return Result.success(companionWalletService.getDashboardStats(userId));
    }

    /**
     * 获取助教收到的订单列表
     */
    @GetMapping("/orders")
    public Result<Page<Order>> getCompanionOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCompanionId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        return Result.success(orderService.page(page, wrapper));
    }

    /**
     * 获取我的钱包信息
     */
    @GetMapping("/wallet")
    public Result<CompanionWallet> getWallet(@AuthenticationPrincipal Long userId) {
        return Result.success(companionWalletService.getWalletByCompanionId(userId));
    }

    /**
     * 获取资金流水
     */
    @GetMapping("/wallet/transactions")
    public Result<List<WalletTransaction>> getTransactions(@AuthenticationPrincipal Long userId) {
        return Result.success(companionWalletService.getTransactions(userId));
    }

    /**
     * 发起提现申请
     */
    @PostMapping("/withdraw")
    public Result<?> applyWithdraw(@AuthenticationPrincipal Long userId, @Valid @RequestBody WithdrawDTO dto) {
        companionWalletService.applyWithdraw(userId, dto);
        return Result.success("提现申请提交成功，请等待管理员审核");
    }

    /**
     * 助教接单
     */
    @PutMapping("/orders/{orderNo}/accept")
    public Result<?> acceptOrder(@AuthenticationPrincipal Long userId, @PathVariable String orderNo) {
        orderService.acceptOrder(userId, orderNo);
        return Result.success("已接单");
    }

    /**
     * 助教拒单
     */
    @PutMapping("/orders/{orderNo}/reject")
    public Result<?> rejectOrder(@AuthenticationPrincipal Long userId, @PathVariable String orderNo, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        orderService.rejectOrder(userId, orderNo, reason);
        return Result.success("已拒单");
    }
}
