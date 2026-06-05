package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色-权限关联表
 */
@Data
@TableName("role_permissions")
public class RolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer roleId;
    private Integer permissionId;
}
