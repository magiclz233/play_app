package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 服务过程时间线事件
 *
 * 事件类型:
 *   1-陪玩出发前往  2-陪玩到达现场  3-用户确认见面  4-服务正式开始
 *   5-中途拍照打卡  6-陪玩申请完工  7-用户确认完工  8-客服介入备注
 *   9-用户发起投诉  10-陪玩发起异常上报
 */
@Data
@TableName(value = "service_timeline_events", autoResultMap = true)
public class ServiceTimelineEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Integer eventType;
    private String eventDesc;

    /** 操作人ID, 0=系统 */
    private Long operatorId;

    /** 1-客户, 2-陪玩, 3-客服, 4-系统 */
    private Integer operatorRole;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ===== 事件类型常量 =====
    public static final int EVENT_COMPANION_DEPARTED  = 1;
    public static final int EVENT_COMPANION_ARRIVED   = 2;
    public static final int EVENT_USER_MET            = 3;
    public static final int EVENT_SERVICE_STARTED     = 4;
    public static final int EVENT_CHECKIN_PHOTO       = 5;
    public static final int EVENT_FINISH_REQUESTED    = 6;
    public static final int EVENT_USER_CONFIRMED      = 7;
    public static final int EVENT_ADMIN_NOTE          = 8;
    public static final int EVENT_USER_COMPLAINT      = 9;
    public static final int EVENT_COMPANION_REPORT    = 10;
}
