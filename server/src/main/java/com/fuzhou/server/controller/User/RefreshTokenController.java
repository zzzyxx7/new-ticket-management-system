package com.fuzhou.server.controller.User;

import com.fuzhou.common.constant.JwtClaimsConstant; // 导入常量类
import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.JwtUtil;
import com.fuzhou.common.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/refresh")
public class RefreshTokenController {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisUtil redisUtil;

    // 前端携带 Refresh Token 申请新的 Access Token
    @PostMapping("/token")
    public Result<Map<String, String>> refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error("Refresh Token 不能为空");
        }

        try {
            // 1. 解析 Refresh Token（验证签名和有效期）
            Claims claims = jwtUtil.parseRefreshToken(refreshToken);

            // 关键改动1：提取完整的用户信息（与老 Token 的 claims 字段一致）
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            String username = claims.get(JwtClaimsConstant.USERNAME).toString();
            String userAccount = claims.get(JwtClaimsConstant.USER_ACCOUNT).toString();
            Integer userStatus = Integer.valueOf(claims.get(JwtClaimsConstant.USER_STATUS).toString());

            // 2. 验证 Refresh Token 是否在 Redis 中存在（防止注销后仍可用）
            Long redisUserId = redisUtil.getUserIdByRefreshToken(refreshToken);
            if (redisUserId == null || !redisUserId.equals(userId)) {
                return Result.error("Refresh Token 已失效，请重新登录");
            }

            // 3. 生成新的 Access Token（复用完整的用户信息，与老 Token 结构一致）
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put(JwtClaimsConstant.USER_ID, userId);
            newClaims.put(JwtClaimsConstant.USERNAME, username);
            newClaims.put(JwtClaimsConstant.USER_ACCOUNT, userAccount);
            newClaims.put(JwtClaimsConstant.USER_STATUS, userStatus);
            String newAccessToken = jwtUtil.generateAccessToken(newClaims);

            // 4. 返回新的 Access Token
            Map<String, String> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            return Result.success(data);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("TOKEN_EXPIRED")) {
                return Result.error("Refresh Token 已过期，请重新登录");
            }
            return Result.error("Refresh Token 无效");
        }
    }

}