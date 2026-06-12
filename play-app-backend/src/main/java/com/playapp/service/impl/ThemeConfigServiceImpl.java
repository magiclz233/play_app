package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.ThemeConfigUpdateDTO;
import com.playapp.entity.SystemConfig;
import com.playapp.mapper.SystemConfigMapper;
import com.playapp.service.ThemeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 主题配置服务实现
 *
 * 配置存储在 system_configs 表中，config_key = "theme_brand"
 * config_value 为 JSONB 格式：
 * {
 *   "brand": {
 *     "primaryColor": "#FF3B5C",
 *     "accentColor": "#FF9F1C",
 *     "gradientEndColor": "#FF7B54"
 *   },
 *   "radius": { "scale": "smooth" },
 *   "spacing": { "scale": "comfortable" },
 *   "typography": { "scale": 1 }
 * }
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements ThemeConfigService {

    private static final String CONFIG_KEY_THEME = "theme_brand";

    /** 默认品牌配置（当数据库无配置时返回） */
    private static final Map<String, Object> DEFAULT_BRAND_CONFIG = Map.of(
        "brand", Map.of(
            "primaryColor", "#FF3B5C",
            "accentColor", "#FF9F1C",
            "gradientEndColor", "#FF7B54"
        ),
        "radius", Map.of("scale", "smooth"),
        "spacing", Map.of("scale", "comfortable"),
        "typography", Map.of("scale", 1)
    );

    @Override
    public Map<String, Object> getPublicConfig() {
        try {
            SystemConfig config = this.getOne(
                new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, CONFIG_KEY_THEME)
                    .eq(SystemConfig::getStatus, 1)
            );

            if (config == null || config.getConfigValue() == null) {
                return DEFAULT_BRAND_CONFIG;
            }

            // 深度合并：数据库值覆盖默认值
            Map<String, Object> merged = new HashMap<>(DEFAULT_BRAND_CONFIG);
            merged.putAll(config.getConfigValue());
            return merged;
        } catch (Exception e) {
            // 表不存在或其他数据库错误时，静默返回默认配置
            log.debug("Failed to load theme config from DB, using defaults: {}", e.getMessage());
            return DEFAULT_BRAND_CONFIG;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(ThemeConfigUpdateDTO dto) {
        // 构建配置 JSON
        Map<String, Object> configValue = new HashMap<>();

        if (dto.getPrimaryColor() != null || dto.getAccentColor() != null
                || dto.getGradientEndColor() != null) {
            Map<String, String> brand = new HashMap<>();
            if (dto.getPrimaryColor() != null) brand.put("primaryColor", dto.getPrimaryColor());
            if (dto.getAccentColor() != null) brand.put("accentColor", dto.getAccentColor());
            if (dto.getGradientEndColor() != null) brand.put("gradientEndColor", dto.getGradientEndColor());
            configValue.put("brand", brand);
        }

        if (dto.getRadiusScale() != null) {
            configValue.put("radius", Map.of("scale", dto.getRadiusScale()));
        }

        if (dto.getSpacingScale() != null) {
            configValue.put("spacing", Map.of("scale", dto.getSpacingScale()));
        }

        if (dto.getFontScale() != null) {
            configValue.put("typography", Map.of("scale", dto.getFontScale()));
        }

        if (configValue.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "至少需要提供一个配置项");
        }

        // 查找或创建配置记录
        SystemConfig config = this.getOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, CONFIG_KEY_THEME)
        );

        if (config == null) {
            // 新建
            config = new SystemConfig();
            config.setConfigKey(CONFIG_KEY_THEME);
            config.setConfigName("品牌主题配置");
            config.setConfigValue(configValue);
            config.setStatus(1);
            config.setRemark("平台品牌色、圆角、间距、字体缩放等视觉配置");
            this.save(config);
        } else {
            // 更新：合并到已有配置中
            Map<String, Object> existing = config.getConfigValue();
            if (existing == null) {
                existing = new HashMap<>();
            }
            // 深度合并
            mergeDeep(existing, configValue);
            config.setConfigValue(existing);
            this.updateById(config);
        }

        log.info("Theme config updated: {}", configValue);
    }

    /**
     * 深度合并两个 Map（mutates target）
     */
    @SuppressWarnings("unchecked")
    private void mergeDeep(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object sourceVal = entry.getValue();
            Object targetVal = target.get(key);

            if (sourceVal instanceof Map && targetVal instanceof Map) {
                Map<String, Object> nestedTarget = new HashMap<>((Map<String, Object>) targetVal);
                mergeDeep(nestedTarget, (Map<String, Object>) sourceVal);
                target.put(key, nestedTarget);
            } else {
                target.put(key, sourceVal);
            }
        }
    }
}
