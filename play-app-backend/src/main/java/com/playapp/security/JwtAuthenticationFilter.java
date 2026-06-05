package com.playapp.security;

import tools.jackson.databind.ObjectMapper;
import com.playapp.common.ErrorCode;
import com.playapp.common.Result;
import com.playapp.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                Claims claims = jwtUtils.getClaimsFromToken(jwt);
                Long userId = claims.get("userId", Long.class);
                Integer roleId = claims.get("role", Integer.class);

                // 检查 Redis 中 token 是否有效（单点登录/踢下线逻辑）
                String redisToken = redisTemplate.opsForValue().get("token:" + userId);
                if (!jwt.equals(redisToken)) {
                    sendErrorResponse(response, ErrorCode.TOKEN_INVALID, "Token已失效或在其他设备登录");
                    return;
                }

                // 这里为了简化 MVP，将 roleId 转换为 Role GrantedAuthority
                String roleName = getRoleName(roleId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(new SimpleGrantedAuthority(roleName)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证上下文", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getRoleName(Integer roleId) {
        if (roleId == null) return "ROLE_USER";
        return switch (roleId) {
            case 1 -> "ROLE_CUSTOMER";
            case 2 -> "ROLE_COMPANION";
            case 3 -> "ROLE_ADMIN";
            default -> "ROLE_USER";
        };
    }

    private void sendErrorResponse(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, msg)));
    }
}
