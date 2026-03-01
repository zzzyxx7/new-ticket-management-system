package com.fuzhou.pojo.vo;

import lombok.Data;

@Data
public class UserLoginVO {
    private long id;
    private String name;             // 用户名
    private String account;          // 账号
    private Integer status;          // 状态
    private String password;         // 密码
}
