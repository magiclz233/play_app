package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户基本信息
 */
@Data
@TableName(value = "users", autoResultMap = true)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;
    private String unionid;
    private String phone;
    private String nickname;
    private String avatarUrl;

    /** 性别: 0-未知, 1-男, 2-女 */
    private Integer gender;
    private String city;

    /** 状态: 1-正常, 2-禁用, 3-注销 */
    private Integer status;

    private LocalDateTime lastLoginTime;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
