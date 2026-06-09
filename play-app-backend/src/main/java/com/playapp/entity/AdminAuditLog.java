package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "admin_audit_logs", autoResultMap = true)
public class AdminAuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;
    private String action;
    private String targetType;
    private String targetId;
    private String remark;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> beforeData;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> afterData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
