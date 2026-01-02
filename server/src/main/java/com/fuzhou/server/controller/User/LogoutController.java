package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.JwtUtil;
import com.fuzhou.common.utils.RedisUtil;
import com.fuzhou.pojo.vo.LoginVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/user/login/logout")
public class LogoutController {
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private RedisUtil redisUtil;
    @Value("${fuzhou.jwt.user-secret-key}")
    private String accessSecretKey;
    private static final String REDIS_KEY_USER_INFO = "user:info:";
    @PostMapping
    public Result<String> logout(@RequestBody LoginVO request) {
        String accessToken = request.getAccess_token();
        String refreshToken = request.getRefresh_token();
        Long userId = BaseContext.getCurrentId();
        try {
            // 1. 将 Access Token 加入黑名单（过期时间=Token剩余有效期，转成秒）
            long expireMillis = jwtUtil.getTokenExpireTime(accessToken, accessSecretKey);
            if (expireMillis > 0) {
                long expireSeconds = expireMillis / 1000;
                redisUtil.addToBlacklist(accessToken, expireSeconds);
            }

            // 2. 删除 Redis 中的 Refresh Token（防止再次刷新）
            if (refreshToken != null && !refreshToken.isEmpty()) {
                redisUtil.deleteRefreshToken(refreshToken);
            }
            // 3. 删除 Redis 中的用户信息
            redisUtil.delete(REDIS_KEY_USER_INFO + userId);

            // 4. 删除当前线程的 UserId
            BaseContext.removeCurrentId();
            return Result.success("注销成功");
        } catch (Exception e) {
            // 即使 Token 已过期，也返回注销成功（避免前端报错）
            return Result.success("注销成功");
        }
    }
}