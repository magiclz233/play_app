package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playapp.common.Result;
import com.playapp.entity.Permission;
import com.playapp.entity.RolePermission;
import com.playapp.mapper.PermissionMapper;
import com.playapp.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器（RBAC）
 */
@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /** 权限树（按 parentId 组装） */
    @GetMapping
    public Result<List<Map<String, Object>>> getPermissionTree() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSortOrder));

        // 构建 id -> node 映射
        Map<Integer, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Permission p : all) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", p.getId());
            node.put("name", p.getName());
            node.put("permissionCode", p.getPermissionCode());
            node.put("parentId", p.getParentId());
            node.put("sortOrder", p.getSortOrder());
            node.put("status", p.getStatus());
            node.put("children", new ArrayList<>());
            nodeMap.put(p.getId(), node);
        }

        // 组装树
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Permission p : all) {
            Map<String, Object> node = nodeMap.get(p.getId());
            if (p.getParentId() == null || p.getParentId() == 0) {
                tree.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(p.getParentId());
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(node);
                } else {
                    tree.add(node); // 父节点不存在，作为根节点
                }
            }
        }
        return Result.success(tree);
    }

    /** 权限平铺列表 */
    @GetMapping("/list")
    public Result<List<Permission>> getPermissionList() {
        return Result.success(permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSortOrder)));
    }

    /** 权限详情 */
    @GetMapping("/{id}")
    public Result<Permission> getPermission(@PathVariable Integer id) {
        return Result.success(permissionMapper.selectById(id));
    }

    /** 创建权限 */
    @PostMapping
    public Result<Permission> createPermission(@RequestBody Permission permission) {
        permissionMapper.insert(permission);
        return Result.success(permission);
    }

    /** 更新权限 */
    @PutMapping("/{id}")
    public Result<?> updatePermission(@PathVariable Integer id, @RequestBody Permission permission) {
        permission.setId(id);
        permissionMapper.updateById(permission);
        return Result.success("修改成功");
    }

    /** 删除权限（同时清理角色-权限关联） */
    @DeleteMapping("/{id}")
    public Result<?> deletePermission(@PathVariable Integer id) {
        // 清理关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getPermissionId, id));
        permissionMapper.deleteById(id);
        return Result.success("已删除");
    }
}
