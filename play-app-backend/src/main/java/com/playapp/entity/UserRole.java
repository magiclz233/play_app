package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户-角色关联表
 */
@Data
@TableName("user_roles")
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Integer roleId;
}
