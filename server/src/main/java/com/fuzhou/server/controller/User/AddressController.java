package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.AddressDTO;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.AddressVO;
import com.fuzhou.server.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址管理（参考 ticket-system）
 * 路径保持 /user/user/map 以兼容现有前端
 */
@RestController
@RequestMapping("/user/user/map")
@Slf4j
public class AddressController {
    @Autowired
    private AddressService addressService;

    /**
     * 获取地址列表（分页，兼容旧接口）
     */
    @GetMapping
    public Result<PageResult> getAddress(PageDTO pageDTO) {
        Long userId = BaseContext.getCurrentId();
        PageResult pageResult = addressService.getAddress(userId, pageDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取地址列表（不分页，供前端地址选择等场景）
     */
    @GetMapping("/list")
    public Result<List<AddressVO>> getAddressList() {
        Long userId = BaseContext.getCurrentId();
        return addressService.getAddressList(userId);
    }

    /**
     * 添加地址
     */
    @PostMapping
    public Result<String> addAddress(@RequestBody AddressDTO dto) {
        Long userId = BaseContext.getCurrentId();
        return addressService.addAddress(dto, userId);
    }

    /**
     * 修改地址
     */
    @PutMapping("/{id}")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody AddressDTO dto) {
        Long userId = BaseContext.getCurrentId();
        return addressService.updateAddress(id, dto, userId);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteAddress(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        return addressService.deleteAddress(id, userId);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{id}/default")
    public Result<String> setDefaultAddress(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        return addressService.setDefaultAddress(id, userId);
    }
}
