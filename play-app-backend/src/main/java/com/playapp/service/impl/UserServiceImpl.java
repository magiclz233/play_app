package com.playapp.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.entity.CompanionProfile;
import com.playapp.entity.User;
import com.playapp.entity.UserRole;
import com.playapp.mapper.CompanionProfileMapper;
import com.playapp.mapper.UserMapper;
import com.playapp.mapper.UserRoleMapper;
import com.playapp.service.UserService;
import com.playapp.utils.JwtUtils;
import com.playapp.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final WxMaService wxMaService;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final CompanionProfileMapper companionProfileMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 从 user_roles 表和现有硬编码逻辑综合获取用户角色列表
     */
    private List<Integer> resolveUserRoles(User user, String phone) {
        // 1. 首选从 user_roles 表查询（数据库驱动的 RBAC）
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        if (userRoles != null && !userRoles.isEmpty()) {
            return userRoles.stream()
                    .map(UserRole::getRoleId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 2. 回退：兼容旧逻辑，根据业务规则自动分配角色
        List<Integer> roles = new ArrayList<>();
        roles.add(1); // 默认客户

        // 检查是否为认证陪玩
        CompanionProfile companion = companionProfileMapper.selectById(user.getId());
        if (companion != null && companion.getAuditStatus() == 1) {
            roles.add(2); // 认证陪玩
        }

        // 检查是否为管理员（硬编码手机号兜底，后续通过 user_roles 表管理）
        if ("admin".equals(phone) || "13800000000".equals(phone)) {
            roles.add(3); // 管理员
        }

        // 3. 同步写入 user_roles 表（自动升级为 DB 驱动）
        List<UserRole> toSave = roles.stream()
                .map(rid -> { UserRole ur = new UserRole(); ur.setUserId(user.getId()); ur.setRoleId(rid); return ur; })
                .collect(Collectors.toList());
        for (UserRole ur : toSave) {
            // 避免重复插入
            if (userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, ur.getUserId())
                    .eq(UserRole::getRoleId, ur.getRoleId())) == 0) {
                userRoleMapper.insert(ur);
            }
        }

        return roles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wxLogin(String code) {
        try {
            // 1. 调用微信接口换取 openid 和 session_key
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            String openid = session.getOpenid();
            String unionid = session.getUnionid(); // 可能为 null

            // 2. 根据 openid 查询用户
            User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

            if (user == null) {
                // 3. 用户不存在则静默注册
                user = new User();
                user.setOpenid(openid);
                user.setUnionid(unionid);
                // 默认角色为普通用户 (可以根据业务需求设置为 1 或其他定义)
                // 这里暂存到 extra 中或专门的关联表，简单起见我们把权限逻辑放到 Token 里，默认为 ROLE_CUSTOMER(1)
                user.setStatus(1); // 1-正常
                user.setLastLoginTime(LocalDateTime.now());
                this.save(user);
            } else {
                if (user.getStatus() != 1) {
                    throw new BusinessException(ErrorCode.USER_DISABLED, "该账号已被禁用");
                }
                user.setLastLoginTime(LocalDateTime.now());
                this.updateById(user);
            }

            // 4. 综合判断角色（优先 user_roles 表，回退业务规则）
            List<Integer> roles = resolveUserRoles(user, user.getPhone());

            // 5. 生成 JWT Token（多角色）
            String token = jwtUtils.generateToken(user.getId(), openid, roles);

            // 6. 将 Token 存入 Redis，实现单点登录/踢出机制 (有效期1天)
            redisTemplate.opsForValue().set("token:" + user.getId(), token, 1, TimeUnit.DAYS);
            
            // 7. 也可以将 session_key 存入 Redis，用于后续解密微信手机号等操作
            redisTemplate.opsForValue().set("wx_session:" + user.getId(), session.getSessionKey(), 1, TimeUnit.DAYS);

            return LoginVO.builder()
                    .token(token)
                    .user(user)
                    .build();

        } catch (Exception e) {
            log.error("微信登录失败, code: {}", code, e);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(ErrorCode.WX_LOGIN_FAILED, "微信登录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO mockLogin(String phone) {
        String mockOpenid = "mock_openid_" + phone;
        
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, mockOpenid));
        if (user == null) {
            user = new User();
            user.setOpenid(mockOpenid);
            user.setPhone(phone);
            user.setNickname("测试用户_" + phone);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            this.save(user);
        } else {
            user.setLastLoginTime(LocalDateTime.now());
            this.updateById(user);
        }

        // 综合判断角色（优先 user_roles 表，回退业务规则）
        List<Integer> roles = resolveUserRoles(user, phone);

        String token = jwtUtils.generateToken(user.getId(), mockOpenid, roles);
        redisTemplate.opsForValue().set("token:" + user.getId(), token, 1, TimeUnit.DAYS);

        return LoginVO.builder()
                .token(token)
                .user(user)
                .build();
    }
}
