package com.fuzhou.server.service.impl;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.AddressDTO;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.entity.UserAddress;
import com.fuzhou.pojo.vo.AddressVO;
import com.fuzhou.server.mapper.AddressMapper;
import com.fuzhou.server.service.AddressService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 地址服务（参考 ticket-system 补全增删改查、设置默认）
 */
@Service
@Slf4j
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;

    @Override
    public PageResult getAddress(Long userId, PageDTO pageDTO) {
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getPageSize());
        List<AddressVO> list = addressMapper.selectByUserId(userId);
        Page<AddressVO> page = (Page<AddressVO>) list;
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Result<List<AddressVO>> getAddressList(Long userId) {
        List<AddressVO> list = addressMapper.selectByUserId(userId);
        return Result.success(list);
    }

    @Override
    public Result<String> addAddress(AddressDTO dto, Long userId) {
        if (!validateAddressDto(dto)) {
            return Result.error("省、市、区、详细地址不能为空");
        }
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailedAddress(dto.getDetailedAddress());
        address.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);
        if (address.getIsDefault() == 1) {
            addressMapper.setAllNonDefault(userId);
        }
        addressMapper.insert(address);
        return Result.success("地址添加成功");
    }

    @Override
    public Result<String> updateAddress(Long id, AddressDTO dto, Long userId) {
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权修改");
        }
        if (!validateAddressDto(dto)) {
            return Result.error("省、市、区、详细地址不能为空");
        }
        existing.setProvince(dto.getProvince());
        existing.setCity(dto.getCity());
        existing.setDistrict(dto.getDistrict());
        existing.setDetailedAddress(dto.getDetailedAddress());
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
            existing.setIsDefault(1);
        }
        addressMapper.update(existing);
        return Result.success("地址修改成功");
    }

    @Override
    public Result<String> deleteAddress(Long id, Long userId) {
        UserAddress address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权删除");
        }
        addressMapper.deleteAddress(id, userId);
        return Result.success("地址删除成功");
    }

    @Override
    public Result<String> setDefaultAddress(Long id, Long userId) {
        UserAddress address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权操作");
        }
        addressMapper.setAllNonDefault(userId);
        addressMapper.setDefaultAddress(id, userId);
        return Result.success("默认地址设置成功");
    }

    private boolean validateAddressDto(AddressDTO dto) {
        return dto != null
                && StringUtils.hasText(dto.getProvince())
                && StringUtils.hasText(dto.getCity())
                && StringUtils.hasText(dto.getDistrict())
                && StringUtils.hasText(dto.getDetailedAddress());
    }
}
