package com.fuzhou.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.properties.JwtProperties;
import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.JwtUtil;
import com.fuzhou.common.utils.RedisUtil;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.server.mapper.UserLoginMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserLoginMapper userLoginMapper;
    @Autowired
    private RedisUtil redisUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (shouldSkipAuthentication(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean optionalAuth = isOptionalAuthPath(requestURI, request.getMethod());

        try {
            String token = extractToken(request);
            if (token == null) {
                if (optionalAuth) {
                    request.setAttribute("isLogin", false);
                    request.setAttribute("loginUserId", null);
                    request.setAttribute("loginMsg", "未登录");
                    filterChain.doFilter(request, response);
                    return;
                }
                sendUnauthorizedResponse(response, "未提供token");
                return;
            }

            if (redisUtil.isInBlacklist(token)) {
                sendUnauthorizedResponse(response, "Token已注销");
                return;
            }

            if (!jwtUtil.validateAccessToken(token)) {
                if (optionalAuth) {
                    request.setAttribute("isLogin", false);
                    request.setAttribute("loginUserId", null);
                    request.setAttribute("loginMsg", "Token无效或已过期");
                    filterChain.doFilter(request, response);
                    return;
                }
                sendUnauthorizedResponse(response, "token无效或已过期");
                return;
            }

            Long userId = jwtUtil.getUserIdFromAccessToken(token);
            String role = jwtUtil.getRoleFromAccessToken(token);

            if (!"ADMIN".equals(role)) {
                User user = userLoginMapper.selectUserById(userId);
                if (user != null && user.getStatus() != null && user.getStatus() == 3) {
                    role = "ADMIN";
                } else if (user == null) {
                    sendForbiddenResponse(response, "用户不存在");
                    return;
                } else if (user.getStatus() != null && user.getStatus() == 2) {
                    sendForbiddenResponse(response, "用户已被禁用");
                    return;
                }
            } else {
                User user = userLoginMapper.selectUserById(userId);
                if (user == null) {
                    sendForbiddenResponse(response, "用户不存在");
                    return;
                }
                if (user.getStatus() != null && user.getStatus() == 2) {
                    sendForbiddenResponse(response, "用户已被禁用");
                    return;
                }
            }

            BaseContext.setCurrentId(userId);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("isLogin", true);
            request.setAttribute("loginUserId", userId);
            request.setAttribute("loginMsg", "已登录");

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.singletonList(authority)
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // 只处理认证相关异常，不吞掉后续业务层抛出的异常（例如 JSON 解析错误）
            sendUnauthorizedResponse(response, "认证失败: " + e.getMessage());
            BaseContext.removeCurrentId();
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            BaseContext.removeCurrentId();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String tokenHeader = jwtProperties.getUserTokenName();
        String value = request.getHeader(tokenHeader);
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private boolean shouldSkipAuthentication(String requestURI) {
        if (requestURI.equals("/user/login/logout") || requestURI.equals("/admin/login/exit")) {
            return false;
        }
        if (requestURI.contains("/user/login") || requestURI.endsWith("/refresh/token") || "/refresh/token".equals(requestURI)) {
            return true;
        }
        if (requestURI.equals("/user/upload")) {
            return true;
        }
        if (requestURI.startsWith("/admin/login")) {
            return true;
        }
        if (requestURI.startsWith("/doc") || requestURI.startsWith("/swagger")
                || requestURI.startsWith("/v2/api-docs") || requestURI.startsWith("/webjars/")) {
            return true;
        }
        return false;
    }

    private boolean isOptionalAuthPath(String requestURI, String method) {
        return "GET".equalsIgnoreCase(method) && "/user/show".equals(requestURI);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<String> result = Result.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<String> result = Result.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
