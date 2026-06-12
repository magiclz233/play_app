package com.playapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.OpLog;
import com.playapp.common.Result;
import com.playapp.dto.AdminRiskHandleDTO;
import com.playapp.entity.RiskReport;
import com.playapp.service.RiskReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/risk/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRiskReportController {

    private final RiskReportService riskReportService;

    @GetMapping
    public Result<Page<RiskReport>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(riskReportService.listAdmin(status, current, size));
    }

    @PutMapping("/{id}")
    @OpLog(module = "RISK", action = "HANDLE", detail = "处理举报: #id")
    public Result<?> handle(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id,
            @Valid @RequestBody AdminRiskHandleDTO dto) {
        riskReportService.adminHandle(adminId, id, dto);
        return Result.success("举报处理已更新");
    }
}
