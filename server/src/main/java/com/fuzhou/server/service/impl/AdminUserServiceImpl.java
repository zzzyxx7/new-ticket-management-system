package com.fuzhou.server.service.impl;

import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.utils.RedisUtil;
import com.fuzhou.pojo.dto.UserPageQueryDTO;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.pojo.vo.AdminUserVO;
import com.fuzhou.server.mapper.AdminUserMapper;
import com.fuzhou.server.service.AdminUserService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {
    @Autowired
    private AdminUserMapper adminUserMapper;

    @Resource
    private RedisUtil redisUtil;

    // Redis缓存Key前缀
    private static final String REDIS_KEY_USER_INFO = "user:info:";


    @Override
    public PageResult pageQuery(UserPageQueryDTO dto) {
        //两个默认值
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
        //使用PageHelper分页
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 查询数据（PageHelper会自动拦截，添加LIMIT）
        Page<AdminUserVO> page = (Page<AdminUserVO>) adminUserMapper.pageQuery(dto);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public AdminUserVO getById(Long id) {
        if (id == null) {
            throw new BaseException("用户ID不能为空");
        }
        AdminUserVO userVO = adminUserMapper.getById(id);
        if (userVO == null) {
            throw new BaseException("用户不存在");
        }

        return userVO;
    }


    @Override
    @Transactional
    public void update(User user) {
        if (user == null || user.getId() == null) {
            throw new BaseException("用户信息或用户ID不能为空");
        }
        AdminUserVO existingUser = adminUserMapper.getById(user.getId());
        if (existingUser == null) {
            throw new BaseException("用户不存在");
        }
        // 更新用户信息
        adminUserMapper.update(user);
        log.info("管理员更新用户信息，用户ID：{}", user.getId());

        // 清除Redis缓存（让下次查询自动加载最新数据）
        String redisKey = REDIS_KEY_USER_INFO + user.getId();
        redisUtil.delete(redisKey);
        log.info("已清除用户Redis缓存，key：{}", redisKey);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BaseException("用户ID不能为空");
    }
        if (status == null || (status != 0 && status != 1 && status != 2)){
            throw new BaseException("状态值非法，必须是 0（禁用）、1（普通用户）或 2（管理员）");
        }
        AdminUserVO existingUser = adminUserMapper.getById(id);
        if (existingUser == null) {
            throw new BaseException("用户不存在");
        }
        adminUserMapper.updateStatus(id, status);
        log.info("管理员更新用户状态，用户ID：{}，新状态：{}", id, status);

        // 清除Redis缓存
        String redisKey = REDIS_KEY_USER_INFO + id;
        redisUtil.delete(redisKey);
        log.info("已清除用户Redis缓存，key：{}", redisKey);

    }





}
