package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 服务过程记录（每笔订单对应一条）
 */
@Data
@TableName(value = "service_records", autoResultMap = true)
public class ServiceRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long companionId;

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    /** 实际服务时长（分钟） */
    private Integer actualDuration;

    private String actualAddress;
    private String finishRemark;

    /** 1-正常完工, 2-提前结束(双方同意), 3-超时结束 */
    private Integer finishType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
