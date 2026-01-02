package com.fuzhou.pojo.entity;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 user 表
 */
@Data
public class User {
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 用户名（默认值：用户未命名）
     */
    private String name;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱（默认值：空字符串）
     */
    private String email;

    /**
     * 状态（tinyint unsigned，默认值：1，可表示 0-未激活/1-正常/2-禁用等）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private String image;

    private String city; //我看数据库里user表有这个，但实体类里面没有这个city
}
