package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.entity.User;
import com.playapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户资料
     */
    @GetMapping("/profile")
    public Result<User> getProfile(@AuthenticationPrincipal Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            // 安全起见，擦除敏感信息（如果有的话，如密码、openid等）
            user.setOpenid(null);
            user.setUnionid(null);
        }
        return Result.success(user);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@AuthenticationPrincipal Long userId, @RequestBody User user) {
        user.setId(userId);
        userService.updateById(user);
        return Result.success("资料更新成功");
    }
}
