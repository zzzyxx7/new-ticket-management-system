package com.fuzhou.server.config;

import com.fuzhou.server.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置（参考 ticket-system）
 * 与 JwtAuthenticationFilter 配合实现 JWT 认证
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // 登出需认证（用于解析 token 设置 BaseContext，便于删除用户缓存）
                .requestMatchers("/user/login/logout", "/admin/login/exit").authenticated()
                // 完全放行：登录、获取验证码、校验验证码、刷新 token、文档（无需登录）
                .requestMatchers("/user/login", "/user/login/**", "/refresh/token").permitAll()
                .requestMatchers("/admin/login/**").permitAll()
                .requestMatchers("/doc/**", "/swagger**", "/v2/api-docs", "/webjars/**").permitAll()
                // 仅首页场次列表可匿名，其余 /user/show/search、/detail、/condition、/buy 等均需认证
                .requestMatchers(HttpMethod.GET, "/user/show", "/user/show/").permitAll()
                // 管理端（除登录外）需 ADMIN 角色
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 其他用户端接口需认证
                .requestMatchers("/user/**").authenticated()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
