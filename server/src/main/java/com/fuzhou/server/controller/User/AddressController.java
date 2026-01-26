package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.AddressVO;
import com.fuzhou.server.service.AddressService;
import jakarta.mail.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/user/map")
@Slf4j
public class AddressController {
    @Autowired
    private AddressService addressService;

    /**
     * 获取用户地址信息
     * @param pageDTO
     * @return
     */
    @GetMapping
    public Result getAddress(PageDTO pageDTO) {
        Long id = BaseContext.getCurrentId();
        log.info("获取用户地址信息");
        PageResult pageResult = addressService.getAddress(id, pageDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除地址
     * @param addressVO
     * @return
     */
    @PostMapping
    public Result update(AddressVO addressVO) {
        Long id = BaseContext.getCurrentId();
        log.info("删除地址信息");
        addressService.deleteAddress(id, addressVO);
        return Result.success();
    }


}
