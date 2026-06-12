package com.playapp.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playapp.entity.OperationLog;
import com.playapp.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 操作日志切面——拦截标注 @OpLog 的方法，自动记录操作日志入库
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OpLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.playapp.common.OpLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog opLog = new OperationLog();
        long start = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OpLog annotation = method.getAnnotation(OpLog.class);

        opLog.setModule(annotation.module());
        opLog.setAction(annotation.action());
        opLog.setDetail(annotation.detail());

        // 目标类型从模块推断
        opLog.setTargetType(annotation.module());

        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            opLog.setUserId(userId);
        }
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            opLog.setUsername(auth.getName());
        }

        // 获取 IP
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                opLog.setIp(ip);
            }
        } catch (Exception ignored) {
            // 非 Web 环境
        }

        // 记录请求参数
        if (annotation.recordParams()) {
            Object[] args = joinPoint.getArgs();
            // 过滤掉 AuthenticationPrincipal 之类的注入参数
            Map<String, Object> paramsMap = new java.util.LinkedHashMap<>();
            String[] paramNames = signature.getParameterNames();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Long || arg instanceof String || arg instanceof Integer || arg instanceof Boolean) {
                    paramsMap.put(paramNames[i], arg);
                } else if (arg != null && !(arg instanceof org.springframework.security.core.Authentication) && !arg.getClass().getName().contains("Authentication")) {
                    try {
                        paramsMap.put(paramNames[i], arg);
                    } catch (Exception ignored2) {
                        paramsMap.put(paramNames[i], arg.toString());
                    }
                }
            }
            opLog.setParams(paramsMap);
        }

        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            opLog.setResult("SUCCESS");
        } catch (Throwable e) {
            opLog.setResult("FAIL");
            opLog.setErrorMsg(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            // 异步写入日志，不阻塞主流程
            try {
                operationLogMapper.insert(opLog);
                log.debug("OpLog: {} {} | {}ms | {}", opLog.getModule(), opLog.getAction(), elapsed, opLog.getResult());
            } catch (Exception e) {
                log.warn("Failed to write operation log: {}", e.getMessage());
            }
        }

        return result;
    }
}
