package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.vo.UserInfoVO;
import com.fuzhou.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/user")
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 获取用户信息
     */
    @GetMapping
    public Result getUserInfo() {
        Long id = BaseContext.getCurrentId();
        UserInfoVO userInfoVO = userInfoService.getUserInfo(id);
        return Result.success(userInfoVO);
    }

    /**
     * 修改用户信息
     */
    // 目前是修改就会删除redis内固定内容，没有缓存，后续看看要不要加
    @PutMapping
    public Result updateUserInfo(@RequestBody UserInfoVO userInfoVO) {
        Long id = BaseContext.getCurrentId();
        userInfoService.updateUserInfo(userInfoVO, id);
        return Result.success();
    }
}
