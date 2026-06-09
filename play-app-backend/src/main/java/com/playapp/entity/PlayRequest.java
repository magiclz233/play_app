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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "play_requests", autoResultMap = true)
public class PlayRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String description;
    private String reserveTime;
    private String address;
    private BigDecimal budget;

    /** 0-待响应, 1-客服跟进中, 2-已成单, 3-已关闭 */
    private Integer status;

    /** 0-未跟进, 1-已联系, 2-已建群, 3-无效需求 */
    private Integer contactStatus;
    private Long assignedAdminId;
    private String adminRemark;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
