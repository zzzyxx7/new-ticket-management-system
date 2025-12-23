package com.fuzhou.server.controller.User;

import com.fuzhou.common.constant.MessageConstant;
import com.fuzhou.common.exception.CodeFailException;
import com.fuzhou.common.exception.CodeNotSendException;
import com.fuzhou.common.utils.MailUtils;
import com.fuzhou.pojo.dto.UserLoginDTO;
import com.fuzhou.pojo.vo.LoginVO;
import com.fuzhou.server.service.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.fuzhou.common.result.Result;

@RestController
@RequestMapping("/user/login")
public class UserLoginController {
    @Autowired
    private UserLoginService userLoginService;
    @Autowired
    private MailUtils mailUtils;

    /*
        目前只有普通登录
     */
    @PostMapping
    public Result login(@RequestBody UserLoginDTO userLoginDTO){
        LoginVO  loginVO = userLoginService.login(userLoginDTO);
        if(loginVO==null || loginVO.getMsg()==null || loginVO.getMsg()=="") return Result.error("登入失败");
        if(loginVO.getMsg() == "成功注册") return Result.success("注册成功");
        return Result.success(loginVO);
    }
    /**
     * 查看账号是否重复
     */
    @GetMapping("/repeat")
    public Result repeat(String account) {
        Boolean user = userLoginService.repeat(account);
        return !user ? Result.success() : Result.error("账号已存在");
    }

    @GetMapping("/phrase")
    public Result sendEmail(String email) {
        //  发送验证码，功能简单，都写在controller里了
        System.out.println("获得验证码");
        String code = mailUtils.sendEmail(email);
        userLoginService.addCode(code,email);
        if (!code.isEmpty()) return Result.success();
        throw new CodeNotSendException(MessageConstant.CODE_ERROR);
    }

    /*

      验证码校验

   */
    @GetMapping("/verify")
    public Result verify(String email,String code) {
        System.out.println("验证码校验");
        Long id = userLoginService.verify(email,code);
        if (id != null) return Result.success(id);
        throw new CodeFailException(MessageConstant.CODE_ERROR_OR_TIMEOUT);
    }



}
