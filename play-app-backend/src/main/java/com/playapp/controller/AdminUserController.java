package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.OpLog;
import com.playapp.common.Result;
import com.playapp.entity.User;
import com.playapp.entity.UserRole;
import com.playapp.mapper.UserRoleMapper;
import com.playapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserRoleMapper userRoleMapper;

    /** 用户列表 */
    @GetMapping
    public Result<Page<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userService.page(page, wrapper));
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setOpenid(null); // 不暴露 openid
            user.setUnionid(null);
        }
        return Result.success(user);
    }

    /** 启用/禁用用户 */
    @PutMapping("/{id}/status")
    @OpLog(module = "USER", action = "STATUS_CHANGE", detail = "用户状态变更: #id")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        User user = userService.getById(id);
        if (user == null) return Result.fail("用户不存在");
        user.setStatus(status);
        userService.updateById(user);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }

    /** 获取用户角色 ID 列表 */
    @GetMapping("/{id}/roles")
    public Result<List<Integer>> getUserRoles(@PathVariable Long id) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .toList();
        return Result.success(roleIds);
    }

    /** 分配用户角色（全量替换） */
    @PutMapping("/{id}/roles")
    @OpLog(module = "USER", action = "ASSIGN_ROLE", detail = "分配用户角色: #id")
    public Result<?> assignUserRoles(@PathVariable Long id, @RequestBody Map<String, List<Integer>> body) {
        List<Integer> roleIds = body.get("roleIds");
        if (roleIds == null) {
            return Result.fail("roleIds 不能为空");
        }
        // 先删除旧角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        // 批量插入新角色
        for (Integer rid : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(id);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
        return Result.success("角色分配成功");
    }
}
