package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.dto.AdminPlayRequestDTO;
import com.playapp.entity.PlayRequest;
import com.playapp.service.PlayRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlayRequestController {

    private final PlayRequestService playRequestService;

    @GetMapping
    public Result<Page<PlayRequest>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<PlayRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PlayRequest::getStatus, status);
        }
        wrapper.orderByDesc(PlayRequest::getCreateTime);
        return Result.success(playRequestService.page(new Page<>(current, size), wrapper));
    }

    @PutMapping("/{id}")
    public Result<?> update(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long id,
            @RequestBody AdminPlayRequestDTO dto) {
        playRequestService.adminUpdate(adminId, id, dto);
        return Result.success("需求跟进已更新");
    }
}
