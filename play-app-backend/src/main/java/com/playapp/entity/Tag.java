package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统标签
 */
@Data
@TableName(value = "tags", autoResultMap = true)
public class Tag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    /** 1-个性标签, 2-技能标签, 3-服务标签 */
    private Integer tagType;

    private String color;
    private Integer sortOrder;
    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
