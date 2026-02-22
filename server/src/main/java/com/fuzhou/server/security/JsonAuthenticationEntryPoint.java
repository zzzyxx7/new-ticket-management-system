package com.fuzhou.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuzhou.common.result.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 未认证时（401）统一返回 JSON 提示，避免空响应体。
 * 风格与 ticket-system / JwtAuthenticationFilter 的 sendUnauthorizedResponse 一致。
 */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        Result<String> result = Result.error("未登录或登录已过期，请重新登录");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
