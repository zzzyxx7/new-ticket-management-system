package com.fuzhou.server.mapper;

import com.fuzhou.pojo.vo.AddressVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AddressMapper {


    List<AddressVO> getAddress(Long id);

    void deleteAddress(Long userId, AddressVO addressVO);
}
