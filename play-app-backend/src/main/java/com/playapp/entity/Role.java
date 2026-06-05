package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 角色表
 */
@Data
@TableName(value = "roles", autoResultMap = true)
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
    private String code;
    private String remark;
    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
