package com.fuzhou.server.service.impl;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.AddressVO;
import com.fuzhou.server.mapper.AddressMapper;
import com.fuzhou.server.service.AddressService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;

    @Override
    public PageResult getAddress(Long id, PageDTO pageDTO) {
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getPageSize());
        List<AddressVO> addressList = addressMapper.getAddress(id);
        Page<AddressVO> page = (Page<AddressVO>) addressList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteAddress(Long userId, AddressVO addressVO) {
        addressMapper.deleteAddress(userId, addressVO);
    }
}
