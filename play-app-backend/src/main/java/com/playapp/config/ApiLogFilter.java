package com.playapp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * API 访问日志过滤器
 * <p>
 * 记录每个请求的完整信息：客户端、方法、路径、参数、body、响应状态、耗时、响应体(错误时)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiLogFilter implements Filter {

    private static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 跳过文件上传请求的 body 缓存（避免内存问题）
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/form-data");

        // 包装请求/响应以支持 body 读取
        HttpServletRequest requestWrapper = isMultipart ? httpRequest : new ContentCachingRequestWrapper(httpRequest, 1024);
        HttpServletResponse responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        // 生成 traceId
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID, traceId);

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String query = httpRequest.getQueryString();

        // 客户端标识
        String userAgent = httpRequest.getHeader("User-Agent");
        String client = parseClient(userAgent);

        // 记录请求
        long start = System.currentTimeMillis();
        log.info("▶ [{}] {} {}{} | client={} | ip={}",
                traceId, method, uri, query != null ? "?" + query : "", client, getClientIp(httpRequest));

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            int status = responseWrapper.getStatus();
            HttpStatus httpStatus = HttpStatus.resolve(status);
            String statusText = httpStatus != null ? httpStatus.getReasonPhrase() : String.valueOf(status);

            // 读取缓存的请求体
            String reqBody = "";
            if (!isMultipart && requestWrapper instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) requestWrapper;
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    reqBody = new String(buf, StandardCharsets.UTF_8);
                    if (reqBody.length() > 500) reqBody = reqBody.substring(0, 500) + "...";
                }
            }

            // 读取缓存的响应体
            String respBody = "";
            int respBodyLen = 0;
            if (responseWrapper instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) responseWrapper;
                byte[] buf = wrapper.getContentAsByteArray();
                respBodyLen = buf.length;
                if (buf.length > 0 && buf.length < 1000) {
                    respBody = new String(buf, StandardCharsets.UTF_8);
                }
                try {
                    wrapper.copyBodyToResponse();
                } catch (IOException e) {
                    // ignore
                }
            }

            // 根据状态码选择日志级别
            boolean isError = status >= 400;
            String arrow = isError ? "✖" : "◀";
            String bodyInfo = reqBody.isEmpty() ? "" : " | body=" + reqBody;
            String respInfo = isError && !respBody.isEmpty() ? " | resp=" + respBody : "";

            if (isError) {
                log.warn("{} [{}] {} {} -> {} {} ({}ms){} | respSize={}B{}",
                        arrow, traceId, method, uri, status, statusText, elapsed, bodyInfo, respBodyLen, respInfo);
            } else {
                log.info("{} [{}] {} {} -> {} {} ({}ms){}",
                        arrow, traceId, method, uri, status, statusText, elapsed, bodyInfo);
            }
            MDC.remove(TRACE_ID);
        }
    }

    /**
     * User-Agent → 简短客户端标识
     */
    private String parseClient(String ua) {
        if (ua == null) return "Unknown";
        StringBuilder sb = new StringBuilder();
        if (ua.contains("Windows")) sb.append("Win");
        else if (ua.contains("Mac")) sb.append("Mac");
        else if (ua.contains("Linux")) sb.append("Linux");
        else if (ua.contains("Android")) sb.append("Android");
        else if (ua.contains("iPhone") || ua.contains("iPad")) sb.append("iOS");
        else sb.append("?");
        sb.append("/");
        if (ua.contains("Edg/")) sb.append("Edge");
        else if (ua.contains("Chrome/")) sb.append("Chrome");
        else if (ua.contains("Safari/") && !ua.contains("Chrome")) sb.append("Safari");
        else if (ua.contains("Firefox/")) sb.append("FF");
        else if (ua.contains("MicroMessenger")) sb.append("WeChat");
        else sb.append("?");
        return sb.toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip != null ? ip : "unknown";
    }

    @Override public void init(FilterConfig config) {}
    @Override public void destroy() {}
}
