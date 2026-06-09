package com.playapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.dto.RiskReportDTO;
import com.playapp.entity.RiskReport;
import com.playapp.service.RiskReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk/reports")
@RequiredArgsConstructor
public class RiskReportController {

    private final RiskReportService riskReportService;

    @PostMapping
    public Result<RiskReport> submit(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RiskReportDTO dto) {
        return Result.success("举报已提交", riskReportService.submit(userId, dto));
    }

    @GetMapping("/mine")
    public Result<Page<RiskReport>> mine(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(riskReportService.listMine(userId, current, size));
    }
}
