package com.fuzhou.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private String name;             // 用户名
    private Integer gender;         // 性别 0-未知 1-男 2-女
    private String account;         // 账号
    private String email;           // 邮箱
    private String image;           // 头像
    private LocalDateTime createTime;// 创建时间
}
