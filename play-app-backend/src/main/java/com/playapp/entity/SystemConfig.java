package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统配置表 — 存储平台级动态配置（品牌主题、功能开关等）
 * 使用 JSONB 字段存储可扩展的配置内容
 */
@Data
@TableName(value = "system_configs", autoResultMap = true)
public class SystemConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键（唯一标识），如 "theme_brand", "theme_layout", "feature_flags" */
    private String configKey;

    /** 配置名称（便于管理） */
    private String configName;

    /** 配置值（JSONB），存储任意结构的配置内容 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> configValue;

    /** 配置说明 */
    private String remark;

    /** 状态: 1-启用, 0-禁用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
