package com.playapp.service;

import com.playapp.dto.ThemeConfigUpdateDTO;

import java.util.Map;

/**
 * 主题配置服务
 */
public interface ThemeConfigService {

    /**
     * 获取公开的品牌主题配置（供前端初始化时拉取）
     * @return 品牌配置 JSON Map
     */
    Map<String, Object> getPublicConfig();

    /**
     * 管理员更新品牌主题配置
     * @param dto 更新请求
     */
    void updateConfig(ThemeConfigUpdateDTO dto);
}
