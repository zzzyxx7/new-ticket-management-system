package com.fuzhou.server.mapper;

import com.fuzhou.pojo.entity.UserAddress;
import com.fuzhou.pojo.vo.AddressVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {

    List<AddressVO> getAddress(@Param("userId") Long userId);

    UserAddress selectById(@Param("id") Long id);

    List<AddressVO> selectByUserId(@Param("userId") Long userId);

    int insert(UserAddress address);

    int update(UserAddress address);

    int deleteById(@Param("id") Long id);

    void deleteAddress(@Param("id") Long id, @Param("userId") Long userId);

    int setAllNonDefault(@Param("userId") Long userId);

    int setDefaultAddress(@Param("id") Long id, @Param("userId") Long userId);

    UserAddress selectDefaultAddress(@Param("userId") Long userId);
}
