package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.WithdrawalRecord;
import com.playapp.mapper.WithdrawalRecordMapper;
import com.playapp.service.CompanionWalletService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/withdrawals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalletController {

    private final CompanionWalletService companionWalletService;
    private final WithdrawalRecordMapper withdrawalRecordMapper;

    /**
     * 获取提现申请列表
     */
    @GetMapping
    public Result<Page<WithdrawalRecord>> getWithdrawalList(
            @RequestParam(required = false) Integer status, // 0-待审核 1-通过 2-拒绝
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<WithdrawalRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<WithdrawalRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(WithdrawalRecord::getStatus, status);
        }
        wrapper.orderByDesc(WithdrawalRecord::getCreateTime);
        
        // 这里可以直接用 mapper 进行简单的分页查询 (或者在Service写一个方法)
        // 确保你的配置里注入了 PaginationInnerInterceptor 才能生效分页
        return Result.success(withdrawalRecordMapper.selectPage(page, wrapper));
    }

    /**
     * 审核提现
     */
    @PutMapping("/{id}/audit")
    public Result<?> auditWithdrawal(@PathVariable Long id, @RequestBody AuditDTO dto) {
        companionWalletService.auditWithdrawal(id, dto.getStatus(), dto.getRemark());
        return Result.success("审核处理成功");
    }
    
    @Data
    public static class AuditDTO {
        private Integer status; // 1-通过 2-拒绝
        private String remark;
    }
}
