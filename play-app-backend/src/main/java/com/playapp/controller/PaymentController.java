package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.PaymentRecord;
import com.playapp.mapper.PaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRecordMapper paymentRecordMapper;

    /** 获取当前用户的支付流水 */
    @GetMapping("/payments")
    public Result<Page<PaymentRecord>> getMyPayments(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<PaymentRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        // 通过订单关联用户ID
        wrapper.apply("order_id IN (SELECT order_id FROM orders WHERE user_id = {0})", userId)
                .orderByDesc(PaymentRecord::getCreateTime);
        return Result.success(paymentRecordMapper.selectPage(page, wrapper));
    }

    /** 陪玩查看自己的收款流水 */
    @GetMapping("/companion/payments")
    public Result<Page<PaymentRecord>> getCompanionPayments(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<PaymentRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("order_id IN (SELECT order_id FROM orders WHERE companion_id = {0})", userId)
                .eq(PaymentRecord::getPaymentType, 4) // 仅结算收款
                .orderByDesc(PaymentRecord::getCreateTime);
        return Result.success(paymentRecordMapper.selectPage(page, wrapper));
    }
}
