package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单状态变更日志（审计追踪）
 */
@Data
@TableName(value = "order_status_logs", autoResultMap = true)
public class OrderStatusLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Integer fromStatus;
    private Integer toStatus;

    /** 操作人ID，0=系统自动 */
    private Long operatorId;

    /** 1-客户, 2-陪玩, 3-客服管理员, 4-系统自动 */
    private Integer operatorRole;

    private String changeReason;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
