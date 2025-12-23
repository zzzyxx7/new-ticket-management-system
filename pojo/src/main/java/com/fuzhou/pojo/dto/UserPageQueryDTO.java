package com.fuzhou.pojo.dto;

import lombok.Data;

@Data
public class UserPageQueryDTO {
    /**
     * 页码（默认第1页）
     */
    private Integer page = 1;

    /**
     * 每页记录数（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 用户名（模糊查询）
     */
    private String name;

    /**
     * 账号（精确查询）
     */
    private String account;

    /**
     * 邮箱（模糊查询）
     */
    private String email;

    /**
     * 状态（0-禁用，1-普通用户，2-管理员）
     */
    private Integer status;
}
