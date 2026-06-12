package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playapp.common.OpLog;
import com.playapp.common.Result;
import com.playapp.entity.Role;
import com.playapp.entity.RolePermission;
import com.playapp.mapper.RoleMapper;
import com.playapp.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器（RBAC）
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /** 角色列表 */
    @GetMapping
    public Result<List<Role>> getRoles() {
        return Result.success(roleMapper.selectList(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getId)));
    }

    /** 角色详情 */
    @GetMapping("/{id}")
    public Result<Role> getRole(@PathVariable Integer id) {
        return Result.success(roleMapper.selectById(id));
    }

    /** 创建角色 */
    @PostMapping
    @OpLog(module = "ROLE", action = "CREATE", detail = "创建角色")
    public Result<Role> createRole(@RequestBody Role role) {
        roleMapper.insert(role);
        return Result.success(role);
    }

    /** 更新角色 */
    @PutMapping("/{id}")
    @OpLog(module = "ROLE", action = "UPDATE", detail = "更新角色: #id")
    public Result<?> updateRole(@PathVariable Integer id, @RequestBody Role role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success("修改成功");
    }

    /** 删除角色 */
    @DeleteMapping("/{id}")
    @OpLog(module = "ROLE", action = "DELETE", detail = "删除角色: #id")
    public Result<?> deleteRole(@PathVariable Integer id) {
        // 删除角色前先清理关联的权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, id));
        roleMapper.deleteById(id);
        return Result.success("已删除");
    }

    /** 获取角色拥有的权限 ID 列表 */
    @GetMapping("/{id}/permissions")
    public Result<List<Integer>> getRolePermissions(@PathVariable Integer id) {
        List<RolePermission> rps = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        List<Integer> permissionIds = rps.stream()
                .map(RolePermission::getPermissionId)
                .toList();
        return Result.success(permissionIds);
    }

    /** 分配角色权限（全量替换） */
    @PutMapping("/{id}/permissions")
    @OpLog(module = "ROLE", action = "ASSIGN_PERM", detail = "分配角色权限: #id")
    public Result<?> assignPermissions(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        List<Integer> permissionIds = body.get("permissionIds");
        if (permissionIds == null) {
            return Result.fail("permissionIds 不能为空");
        }
        // 先删除旧权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, id));
        // 批量插入新权限
        for (Integer permId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(id);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
        return Result.success("权限分配成功");
    }
}
