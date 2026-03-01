package com.fuzhou.pojo.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String access_token;     // 访问令牌
    private String refresh_token;   // 刷新令牌
    private String msg;             // 提示信息
}
