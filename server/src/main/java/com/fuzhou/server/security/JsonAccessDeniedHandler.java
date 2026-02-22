package com.fuzhou.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuzhou.common.result.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 无权限时（403）统一返回 JSON 提示，避免空响应体。
 * 风格与 ticket-system / JwtAuthenticationFilter 的 sendForbiddenResponse 一致。
 */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        Result<String> result = Result.error("无权限访问");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
