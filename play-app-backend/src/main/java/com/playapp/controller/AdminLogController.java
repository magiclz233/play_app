package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.OperationLog;
import com.playapp.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志查询控制器
 */
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    private final OperationLogMapper operationLogMapper;

    /**
     * 分页查询操作日志（支持多条件筛选）
     */
    @GetMapping
    public Result<Page<OperationLog>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        Page<OperationLog> page = new Page<>(current, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (username != null && !username.isBlank()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (module != null && !module.isBlank()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (action != null && !action.isBlank()) {
            wrapper.eq(OperationLog::getAction, action);
        }
        if (result != null && !result.isBlank()) {
            wrapper.eq(OperationLog::getResult, result);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(OperationLog::getDetail, keyword)
                    .or().like(OperationLog::getTargetId, keyword)
                    .or().like(OperationLog::getUsername, keyword));
        }
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        return Result.success(operationLogMapper.selectPage(page, wrapper));
    }

    /**
     * 获取模块列表（用于筛选下拉）
     */
    @GetMapping("/modules")
    public Result<java.util.List<String>> modules() {
        java.util.List<OperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>()
                        .select(OperationLog::getModule)
                        .groupBy(OperationLog::getModule));
        java.util.List<String> modules = logs.stream()
                .map(OperationLog::getModule)
                .distinct()
                .toList();
        return Result.success(modules);
    }
}
