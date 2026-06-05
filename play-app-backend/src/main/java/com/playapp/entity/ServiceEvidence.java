package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 服务凭证/证据表
 */
@Data
@TableName(value = "service_evidence", autoResultMap = true)
public class ServiceEvidence implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long timelineEventId;

    /** 文件类型: 1-图片, 2-视频, 3-语音 */
    private Integer fileType;
    private String fileUrl;
    private String thumbnailUrl;
    private Long uploaderId;

    /** 上传角色: 1-客户, 2-陪玩, 3-客服 */
    private Integer uploaderRole;
    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
