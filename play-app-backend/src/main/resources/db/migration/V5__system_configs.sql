-- ================================================================
-- V5: 系统配置表（品牌主题、功能开关等动态配置）
-- ================================================================

CREATE TABLE IF NOT EXISTS system_configs (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_name VARCHAR(100) NOT NULL,
    config_value JSONB DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_configs_key ON system_configs(config_key);

COMMENT ON TABLE system_configs IS '系统配置表 — 平台动态配置';
COMMENT ON COLUMN system_configs.config_key IS '配置键: theme_brand, theme_layout, feature_flags';
COMMENT ON COLUMN system_configs.config_name IS '配置名称';
COMMENT ON COLUMN system_configs.config_value IS '配置值(JSONB)';
COMMENT ON COLUMN system_configs.remark IS '说明';
COMMENT ON COLUMN system_configs.status IS '1-启用 0-禁用';

-- 插入默认品牌主题配置
INSERT INTO system_configs (config_key, config_name, config_value, remark, status)
VALUES (
    'theme_brand',
    '品牌主题配置',
    '{
        "brand": {
            "primaryColor": "#FF3B5C",
            "accentColor": "#FF9F1C",
            "gradientEndColor": "#FF7B54"
        },
        "radius": {"scale": "smooth"},
        "spacing": {"scale": "comfortable"},
        "typography": {"scale": 1}
    }'::jsonb,
    '品牌色、圆角、间距、字体缩放。GET /api/public/theme-config',
    1
)
ON CONFLICT (config_key) DO NOTHING;
