package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.playapp.common.Result;
import com.playapp.entity.User;
import com.playapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

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
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        User user = userService.getById(id);
        if (user == null) return Result.fail("用户不存在");
        user.setStatus(status);
        userService.updateById(user);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }
}
