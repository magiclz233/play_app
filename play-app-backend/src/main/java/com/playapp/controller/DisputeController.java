package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.dto.DisputeCreateDTO;
import com.playapp.dto.DisputeResolveDTO;
import com.playapp.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DisputeController {

    private final OrderService orderService;

    /** 用户发起纠纷 */
    @PostMapping("/api/orders/{orderNo}/dispute")
    public Result<?> createDispute(@AuthenticationPrincipal Long userId, @Valid @RequestBody DisputeCreateDTO dto) {
        dto.setOrderNo(dto.getOrderNo()); // ensure from path
        orderService.createDispute(userId, "customer", dto);
        return Result.success("纠纷申诉已提交");
    }

    /** 管理员处理纠纷 */
    @PutMapping("/api/admin/orders/dispute")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> resolveDispute(@AuthenticationPrincipal Long adminId, @Valid @RequestBody DisputeResolveDTO dto) {
        orderService.resolveDispute(adminId, dto);
        return Result.success("纠纷已处理");
    }
}
