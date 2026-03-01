package com.fuzhou.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String name;             // 用户名
    private Integer gender;         // 性别 0-未知 1-男 2-女
    private String account;         // 账号
    private String email;           // 邮箱
    private Integer status;         // 状态 0-禁用 1-普通 2-管理员
    private String image;           // 头像
    private LocalDateTime createTime;// 创建时间
    private String city;            // 城市
}
