package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.AddressVO;

public interface AddressService {
    PageResult getAddress(Long id, PageDTO pageDTO);

    void deleteAddress(Long id, AddressVO addressVO);
}
