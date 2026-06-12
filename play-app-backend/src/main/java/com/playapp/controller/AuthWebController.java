package com.playapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.playapp.common.Result;
import com.playapp.entity.Permission;
import com.playapp.entity.Role;
import com.playapp.entity.RolePermission;
import com.playapp.entity.User;
import com.playapp.entity.UserRole;
import com.playapp.mapper.PermissionMapper;
import com.playapp.mapper.RoleMapper;
import com.playapp.mapper.RolePermissionMapper;
import com.playapp.mapper.UserRoleMapper;
import com.playapp.utils.JwtUtils;
import com.playapp.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * PC Web 管理端认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final com.playapp.service.UserService userService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    /**
     * PC Web 端账号密码登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.fail("用户名和密码不能为空");
        }

        // 查找用户
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.fail("账号已被禁用");
        }
        if (user.getPasswordHash() == null) {
            return Result.fail("该账号未设置密码，请使用微信登录");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.fail("用户名或密码错误");
        }

        // 获取用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        List<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId).distinct().toList();
        
        if (roleIds.isEmpty()) {
            return Result.fail("无权限登录后台系统");
        }
        
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().in(Role::getId, roleIds));
        
        boolean isAdmin = roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getCode()));
        if (!isAdmin) {
            return Result.fail("只有管理员可以登录后台系统");
        }

        // 生成 JWT
        String token = jwtUtils.generateToken(user.getId(), user.getOpenid(), roleIds);

        // 存入 Redis
        redisTemplate.opsForValue().set("token:" + user.getId(), token, 1, TimeUnit.DAYS);

        // 更新登录时间
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userService.updateById(user);

        LoginVO loginVO = LoginVO.builder()
                .token(token)
                .user(user)
                .build();
        return Result.success("登录成功", loginVO);
    }

    /**
     * 获取当前登录用户信息（含角色和权限列表）
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return Result.fail(401, "未认证");
        }

        User user = userService.getById(userId);
        if (user != null) {
            user.setOpenid(null);
            user.setUnionid(null);
            user.setPasswordHash(null);
        }

        // 获取角色列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        List<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();

        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>().in(Role::getId, roleIds));

        // 获取权限列表（通过角色间接获取）
        Set<Integer> permIdSet = new LinkedHashSet<>();
        for (Integer rid : roleIds) {
            rolePermissionMapper.selectList(
                    new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, rid))
                    .forEach(rp -> permIdSet.add(rp.getPermissionId()));
        }
        List<String> permissions = new ArrayList<>();
        if (!permIdSet.isEmpty()) {
            permissions = permissionMapper.selectList(
                    new LambdaQueryWrapper<Permission>().in(Permission::getId, permIdSet))
                    .stream()
                    .map(Permission::getPermissionCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", permissions);
        return Result.success(result);
    }
}
