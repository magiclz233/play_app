package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户评价表
 */
@Data
@TableName(value = "reviews", autoResultMap = true)
public class Review implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long userId;
    private Long companionId;

    /** 评分: 1-5星 */
    private Integer rating;
    private String content;

    /** 评价图片URL列表(逗号分隔) */
    private String images;

    /** 评价标签ID列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> tagIds;

    private Boolean isAnonymous;
    private String replyContent;
    private LocalDateTime replyTime;

    /** 状态: 1-正常, 2-隐藏, 3-违规屏蔽 */
    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
