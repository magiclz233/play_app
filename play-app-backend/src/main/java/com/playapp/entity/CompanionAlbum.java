package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 陪玩相册图片
 */
@Data
@TableName(value = "companion_albums", autoResultMap = true)
public class CompanionAlbum implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companionId;
    private String imageUrl;
    private String thumbnailUrl;
    private Integer sortOrder;
    private Boolean isCover;

    /** 0-待审核, 1-正常, 2-违规屏蔽 */
    private Integer auditStatus;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
