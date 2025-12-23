package com.fuzhou.server.controller.Admin;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.UserPageQueryDTO;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.pojo.vo.AdminUserVO;
import com.fuzhou.server.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@Slf4j
public class AdminUserController {
    @Autowired
    private AdminUserService adminUserService;

    //分页查询用户信息
    @GetMapping
    public Result<PageResult> pageQuery(UserPageQueryDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]分页查询用户列表，条件：{}", adminId, dto);
        PageResult pageResult = adminUserService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/detail")
    public Result<AdminUserVO> getById(Long id) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]查询用户详情，用户ID：{}", adminId, id);
        AdminUserVO userVO = adminUserService.getById(id);
        return Result.success(userVO);
    }
    @PutMapping
    public Result update(@RequestBody User user) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]修改用户信息，用户信息：{}", adminId, user);
        adminUserService.update(user);
        return Result.success("修改成功");
    }

    @PutMapping("/control")
    public Result updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]更新用户状态，用户ID：{}，新状态：{}", adminId, id, status);
        adminUserService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
