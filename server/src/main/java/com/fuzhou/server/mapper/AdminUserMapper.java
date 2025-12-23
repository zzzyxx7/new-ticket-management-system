package com.fuzhou.server.mapper;

import com.fuzhou.pojo.dto.UserPageQueryDTO;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.pojo.vo.AdminUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface AdminUserMapper {
    //分页查询用户信息
    List<AdminUserVO> pageQuery(UserPageQueryDTO dto);
    //查询用户详情
    AdminUserVO getById(@Param("id") Long id);
    //更新用户
    void update(User user);
    //启用/禁用用户
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
