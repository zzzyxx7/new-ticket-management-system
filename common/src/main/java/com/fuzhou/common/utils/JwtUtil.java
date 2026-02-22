package com.fuzhou.common.utils;

import com.fuzhou.common.constant.JwtClaimsConstant;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    // Access Token 配置
    @Value("${fuzhou.jwt.user-secret-key}")
    private String accessSecretKey; // Base64 密钥
    @Value("${fuzhou.jwt.user-ttl}")
    private long accessTtl;

    // Refresh Token 配置（单独密钥）
    @Value("${fuzhou.jwt.refresh-secret-key}")
    private String refreshSecretKey; // Base64 密钥
    @Value("${fuzhou.jwt.refresh-ttl}")
    private long refreshTtl;

    // ------------------------------ Access Token 方法 ------------------------------
    public  String generateAccessToken(Map<String, Object> claims) {
        return generateToken(claims, accessSecretKey, accessTtl);
    }

    public  Claims parseAccessToken(String token) {
        return parseToken(token, accessSecretKey);
    }

    // ------------------------------ Refresh Token 方法 ------------------------------
    public String generateRefreshToken(Map<String, Object> claims) {
        return generateToken(claims, refreshSecretKey, refreshTtl);
    }

    public Claims parseRefreshToken(String token) {
        return parseToken(token, refreshSecretKey);
    }

    // ------------------------------ 通用方法 ------------------------------
    /**
     * 生成 Token（Access/Refresh 通用）
     */
    private String generateToken(Map<String, Object> claims, String base64SecretKey, long ttl) {
        // Base64 解码密钥
        byte[] keyBytes = Base64.getDecoder().decode(base64SecretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        // 过期时间
        Date expiration = new Date(System.currentTimeMillis() + ttl);

        return Jwts.builder()
                .claims(claims) // 存入用户ID等核心信息
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token（Access/Refresh 通用）
     */
    private Claims parseToken(String token, String base64SecretKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64SecretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("TOKEN_EXPIRED", e);
        } catch (SignatureException e) {
            throw new RuntimeException("SIGNATURE_INVALID", e);
        } catch (Exception e) {
            throw new RuntimeException("TOKEN_INVALID", e);
        }
    }

    // 获取 Token 过期时间（用于黑名单存储）
    public long getTokenExpireTime(String token, String base64SecretKey) {
        Claims claims = parseToken(token, base64SecretKey);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 验证 Access Token 是否有效
     */
    public boolean validateAccessToken(String token) {
        try {
            parseAccessToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Access Token 获取用户ID
     */
    public Long getUserIdFromAccessToken(String token) {
        Claims claims = parseAccessToken(token);
        Object userId = claims.get(JwtClaimsConstant.USER_ID);
        if (userId == null) return null;
        return Long.valueOf(userId.toString());
    }

    /**
     * 从 Access Token 获取角色（USER/ADMIN），旧 token 无 role 时返回 USER
     */
    public String getRoleFromAccessToken(String token) {
        Claims claims = parseAccessToken(token);
        String role = claims.get(JwtClaimsConstant.ROLE, String.class);
        return role != null ? role : "USER";
    }
}