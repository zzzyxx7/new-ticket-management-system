package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.UserPageQueryDTO;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.pojo.vo.AdminUserVO;


public interface AdminUserService {
    PageResult pageQuery(UserPageQueryDTO dto);

    AdminUserVO getById(Long id);

    void update(User user);

    void updateStatus(Long id, Integer status);
}
