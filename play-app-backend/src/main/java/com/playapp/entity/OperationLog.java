package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理员操作日志
 */
@Data
@TableName(value = "operation_logs", autoResultMap = true)
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> params;

    private String result;
    private String errorMsg;
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
