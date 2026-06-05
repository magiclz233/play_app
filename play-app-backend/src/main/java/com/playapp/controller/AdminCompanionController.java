package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.CompanionProfile;
import com.playapp.service.CompanionProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companions")
@RequiredArgsConstructor
public class AdminCompanionController {

    private final CompanionProfileService companionProfileService;

    /**
     * 获取待审核的助教列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<CompanionProfile>> getPendingList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<CompanionProfile> page = new Page<>(current, size);
        LambdaQueryWrapper<CompanionProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanionProfile::getAuditStatus, 0)
               .orderByDesc(CompanionProfile::getCreateTime);
               
        return Result.success(companionProfileService.page(page, wrapper));
    }

    /**
     * 审核通过或驳回
     */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> audit(@PathVariable Long id, @RequestBody AuditRequest req) {
        companionProfileService.auditApplication(id, req.getIsPass(), req.getRejectReason());
        return Result.success("审核操作成功");
    }

    @Data
    public static class AuditRequest {
        private Boolean isPass;
        private String rejectReason;
    }
}
