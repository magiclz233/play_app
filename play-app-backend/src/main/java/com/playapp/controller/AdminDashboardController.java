package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playapp.common.Result;
import com.playapp.entity.*;
import com.playapp.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final OrderMapper orderMapper;
    private final CompanionProfileMapper companionProfileMapper;
    private final UserMapper userMapper;
    private final WithdrawalRecordMapper withdrawalRecordMapper;

    @GetMapping
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, LocalDate.now().atStartOfDay()));
        data.put("todayOrderCount", todayOrders);

        // 今日收入（已结算订单的平台费）
        var settledOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, Order.STATUS_SETTLED)
                .ge(Order::getFinishTime, LocalDate.now().atStartOfDay()));
        BigDecimal todayRevenue = settledOrders.stream()
                .map(o -> o.getPlatformFee() != null ? o.getPlatformFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("todayRevenue", todayRevenue);

        // 待审核入驻
        Long pendingAudit = companionProfileMapper.selectCount(
                new LambdaQueryWrapper<CompanionProfile>().eq(CompanionProfile::getAuditStatus, 0));
        data.put("pendingAuditCount", pendingAudit);

        // 待处理提现
        Long pendingWithdraw = withdrawalRecordMapper.selectCount(
                new LambdaQueryWrapper<WithdrawalRecord>().eq(WithdrawalRecord::getStatus, 0));
        data.put("pendingWithdrawCount", pendingWithdraw);

        // 活跃陪玩数
        Long activeCompanions = companionProfileMapper.selectCount(
                new LambdaQueryWrapper<CompanionProfile>().eq(CompanionProfile::getAuditStatus, 1));
        data.put("activeCompanionCount", activeCompanions);

        // 注册用户数
        Long totalUsers = userMapper.selectCount(null);
        data.put("totalUserCount", totalUsers);

        return Result.success(data);
    }
}
