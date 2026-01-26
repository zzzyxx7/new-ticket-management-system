package com.fuzhou.server.mapper;

import com.fuzhou.pojo.dto.UserLoginDTO;
import com.fuzhou.pojo.entity.Code;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.pojo.vo.UserLoginVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface UserLoginMapper {
    void register(UserLoginDTO userLoginDTO);

    void addCode(String code, String email, LocalDateTime time);

    Code verify(String email);

    void changeUsed(Integer id);

    UserLoginVO passwordLogin(UserLoginDTO userLoginDTO);

    Long getUserId(String email);

    User selectUserById(Long userId);

    UserLoginVO getUser(String email);

    Boolean repeat(String account);

    UserLoginVO AdminLogin(UserLoginDTO userLoginDTO);
}
