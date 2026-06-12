package com.playapp.controller;

import com.playapp.common.OpLog;
import com.playapp.common.Result;
import com.playapp.dto.ThemeConfigUpdateDTO;
import com.playapp.service.ThemeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 主题配置控制器
 *
 * GET  /api/public/theme-config   — 公开接口，前端拉取品牌主题配置
 * PUT  /api/admin/theme-config    — 管理接口，管理员更新品牌色/布局
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ThemeConfigController {

    private final ThemeConfigService themeConfigService;

    /**
     * 获取公开品牌主题配置（无需认证）
     * 前端在 App.vue onLaunch 时调用此接口获取品牌色等配置
     */
    @GetMapping("/public/theme-config")
    public Result<Map<String, Object>> getPublicConfig() {
        Map<String, Object> config = themeConfigService.getPublicConfig();
        return Result.success(config);
    }

    /**
     * 管理员更新品牌主题配置
     */
    @PutMapping("/admin/theme-config")
    @PreAuthorize("hasRole('ADMIN')")
    @OpLog(module = "SYSTEM", action = "UPDATE", detail = "更新主题配置")
    public Result<Void> updateConfig(@Valid @RequestBody ThemeConfigUpdateDTO dto) {
        themeConfigService.updateConfig(dto);
        return Result.success();
    }
}
