package com.playapp.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 — 所有异常统一捕获、打印完整堆栈、返回 Result 给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String ctx(HttpServletRequest req) {
        return MDC.get("traceId") + " " + req.getMethod() + " " + req.getRequestURI();
    }

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[BIZ] {} | code={} msg={}", ctx(request), e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[VALID] {} | {}", ctx(request), msg, e);
        return Result.fail(ErrorCode.PARAM_ERROR, msg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[BIND] {} | {}", ctx(request), msg, e);
        return Result.fail(ErrorCode.PARAM_ERROR, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("[MISSING_PARAM] {} | {}", ctx(request), e.getParameterName(), e);
        return Result.fail(ErrorCode.PARAM_ERROR, "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("[BAD_BODY] {} | {}", ctx(request), e.getMessage(), e);
        return Result.fail(ErrorCode.PARAM_ERROR, "请求体格式错误，请检查JSON格式");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[CONSTRAINT] {} | {}", ctx(request), msg, e);
        return Result.fail(ErrorCode.PARAM_ERROR, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("[ARG] {} | {}", ctx(request), e.getMessage(), e);
        return Result.fail(ErrorCode.PARAM_ERROR, e.getMessage());
    }

    // ==================== 认证 / 授权异常 ====================

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthException(AuthenticationException e, HttpServletRequest request) {
        log.warn("[AUTH] {} | {}", ctx(request), e.getMessage(), e);
        return Result.fail(ErrorCode.UNAUTHORIZED, "请先登录");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("[FORBIDDEN] {} | {}", ctx(request), e.getMessage(), e);
        return Result.fail(ErrorCode.FORBIDDEN, "无权限访问");
    }

    // ==================== 路由 / 方法异常 ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("[METHOD] {} | supported={}", ctx(request), e.getSupportedHttpMethods(), e);
        return Result.fail(ErrorCode.PARAM_ERROR, "不支持的请求方式: " + request.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("[MEDIA] {} | {}", ctx(request), e.getContentType(), e);
        return Result.fail(ErrorCode.PARAM_ERROR, "不支持的Content-Type");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFound(Exception e, HttpServletRequest request) {
        log.warn("[404] {}", ctx(request), e);
        return Result.fail(ErrorCode.NOT_FOUND, "接口不存在: " + request.getRequestURI());
    }

    // ==================== 兜底：未知异常 ====================

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleUnknownException(Exception e, HttpServletRequest request) {
        log.error("[UNKNOWN] {} | {}", ctx(request), e.getMessage(), e);
        String traceId = MDC.get("traceId");
        return Result.fail(ErrorCode.SERVER_ERROR,
                String.format("系统内部错误 (traceId: %s)", traceId != null ? traceId : "N/A"));
    }
}
