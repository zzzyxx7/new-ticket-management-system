package com.fuzhou.server.service.impl;

import com.fuzhou.common.constant.JwtClaimsConstant;
import com.fuzhou.common.exception.AccountOrPasswordException;
import com.fuzhou.common.exception.AccountUnExistException;
import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.properties.JwtProperties;
import com.fuzhou.common.utils.*;
import com.fuzhou.pojo.dto.UserLoginDTO;
import com.fuzhou.pojo.entity.Code;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.pojo.vo.LoginVO;
import com.fuzhou.pojo.vo.UserLoginVO;
import com.fuzhou.server.mapper.UserLoginMapper;
import com.fuzhou.server.service.UserLoginService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableAsync
public class UserLoginServiceImpl implements UserLoginService {
    @Autowired
    private UserLoginMapper userLoginMapper;
    @Resource
    private RedisUtil redisUtil;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private JwtUtil jwtUtil;

    PasswordUtil passwordUtil = new PasswordUtil();

    // 缓存Key前缀（遵循命名规则）
    private static final String USER_CACHE_KEY = "user:info:";
    // 缓存过期时间：2小时（7200秒）
    private static final long CACHE_EXPIRE_SECONDS = 7200;

    @Override
    @Transactional
    public LoginVO login(UserLoginDTO userLoginDTO) {
        Long userId = 0L;
        LoginVO loginVO = new LoginVO();
        if(userLoginDTO.getType()==1){
            // 注册前先校验邮箱是否已被使用（仅校验非空邮箱）
            String email = userLoginDTO.getEmail();
            if (email != null && !email.isEmpty()) {
                Boolean emailUsed = userLoginMapper.emailRepeat(email);
                if (Boolean.TRUE.equals(emailUsed)) {
                    throw new BaseException("该邮箱已被其他账号使用");
                }
            }
            SnowflakeIdUtil snowflakeIdUtil = SnowflakeIdUtil.getDefaultInstance();
            userLoginDTO.setId(snowflakeIdUtil.generateId());
            userLoginDTO.setCreateTime(LocalDateTime.now());
            userLoginDTO.setPassword(passwordUtil.encryptPassword(userLoginDTO.getPassword()));
            userLoginMapper.register(userLoginDTO);
            userId = userLoginDTO.getId();
            cacheUserInfo(userId);
            loginVO.setMsg("注册成功");
            return loginVO;
        }
        if (userLoginDTO.getType() == 2) {
            UserLoginVO userLoginVO = userLoginMapper.passwordLogin(userLoginDTO);
            if (userLoginVO == null) {
                throw new AccountOrPasswordException("账号或密码错误");
            }
            // 账号状态检查：status=2 表示被禁用
            if (userLoginVO.getStatus() != null && userLoginVO.getStatus() == 2) {
                throw new BaseException("用户已被禁用，请联系管理员");
            }
            if (passwordUtil.verifyPassword(userLoginDTO.getPassword(), userLoginVO.getPassword())) {
                userId = userLoginVO.getId();
                cacheUserInfo(userId);
                loginVO = setToken(userLoginVO, loginVO, "USER");
                return loginVO;
            } else {
                throw new AccountOrPasswordException("账号或密码错误");
            }
        }
        if(userLoginDTO.getType()==3){
            UserLoginVO userLoginVO = userLoginMapper.getUser(userLoginDTO.getEmail());
            userId = userLoginVO.getId();
            if(userId==null || userId == 0L) throw new AccountUnExistException("账号不存在");
            userId = verify(userLoginDTO.getEmail(), userLoginDTO.getCode());
            if(userId==null || userId == 0L) throw new AccountUnExistException("验证码错误或超时");
            cacheUserInfo(userId);
            loginVO = setToken(userLoginVO, loginVO, "USER");
            return loginVO;
        }

        if (userLoginDTO.getType() == 10) {
            UserLoginVO userLoginVO = userLoginMapper.AdminLogin(userLoginDTO);
            if (userLoginVO == null) {
                throw new AccountOrPasswordException("账号或密码错误");
            }
            // 管理员账号状态检查：status=2 表示被禁用
            if (userLoginVO.getStatus() != null && userLoginVO.getStatus() == 2) {
                throw new BaseException("管理员账号已被禁用，请联系超级管理员");
            }
            if (passwordUtil.verifyPassword(userLoginDTO.getPassword(), userLoginVO.getPassword())) {
                userId = userLoginVO.getId();
                cacheUserInfo(userId);
                loginVO = setToken(userLoginVO, loginVO, "ADMIN");
                return loginVO;
            } else {
                throw new AccountOrPasswordException("账号或密码错误");
            }
        }
        return loginVO;
    }

    @Override
    public void addCode(String code, String email) {
        LocalDateTime time = LocalDateTime.now().plusMinutes(5);
        userLoginMapper.addCode(code, email, time);
    }

    @Override
    @Transactional
    public Long verify(String email, String code) {
        Code verificationCodes = userLoginMapper.verify(email);

        if(verificationCodes == null) return null;

        Boolean a = verificationCodes.getCode().equals(code) &&
                verificationCodes.getExpireTime().isAfter(LocalDateTime.now()) &&
                !verificationCodes.getUsed()
                ;

        Long userId = null;
        if(a) {
            userLoginMapper.changeUsed(verificationCodes.getId());
            userId = userLoginMapper.getUserId(email);
        }

        return userId;
    }

    @Override
    public Boolean repeat(String account) {
        return userLoginMapper.repeat(account);
    }


    @Async
    public void cacheUserInfo(Long userId) {
        if (userId == null) return;

        String cacheKey = USER_CACHE_KEY + userId;
        // 先检查缓存是否已存在（未过期），存在则无需覆盖
        if (redisUtil.hasKey(cacheKey) && redisUtil.getExpire(cacheKey) > 60) { // 剩余过期时间>60秒则不更新
            System.out.println("用户缓存已存在，无需重复缓存：userId=" + userId);
            return;
        }

        // 缓存不存在/即将过期，再查数据库并缓存
        User user = userLoginMapper.selectUserById(userId);
        if (user != null) {
            redisUtil.set(cacheKey, user, CACHE_EXPIRE_SECONDS);
        }
    }


    public LoginVO setToken(UserLoginVO user, LoginVO loginVO, String role){
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        claims.put(JwtClaimsConstant.USERNAME,user.getName());
        claims.put(JwtClaimsConstant.USER_ACCOUNT, user.getAccount());
        claims.put(JwtClaimsConstant.USER_STATUS, user.getStatus());
        claims.put(JwtClaimsConstant.ROLE, role);
        loginVO.setAccess_token(jwtUtil.generateAccessToken(claims));
        loginVO.setRefresh_token(jwtUtil.generateRefreshToken(claims));
        redisUtil.saveRefreshToken(loginVO.getRefresh_token(), user.getId(), jwtProperties.getUserTtl());
        loginVO.setMsg("登录成功");
        return loginVO;
    }
}
