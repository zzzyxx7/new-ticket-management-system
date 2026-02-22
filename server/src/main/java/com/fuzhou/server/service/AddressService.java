package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.AddressDTO;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.AddressVO;

import java.util.List;

public interface AddressService {
    PageResult getAddress(Long userId, PageDTO pageDTO);

    Result<List<AddressVO>> getAddressList(Long userId);

    Result<String> addAddress(AddressDTO dto, Long userId);

    Result<String> updateAddress(Long id, AddressDTO dto, Long userId);

    Result<String> deleteAddress(Long id, Long userId);

    Result<String> setDefaultAddress(Long id, Long userId);
}
