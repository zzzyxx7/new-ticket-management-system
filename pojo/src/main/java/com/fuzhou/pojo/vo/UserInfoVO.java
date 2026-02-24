package com.fuzhou.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private String name;
    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;
    private String account;
    private String email;
    private String image;
    private LocalDateTime createTime;
}
