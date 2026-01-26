package com.fuzhou.pojo.entity;


import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户地址表实体类
 */
@Data

public class UserAddress {

    /**
     * 地址主键ID
     */

    private Long id;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 具体详细地址（不含省市区）
     */
    private String detailedAddress;

    /**
     * 完整地址（省市区+具体地址，数据库生成列，无需手动维护）
     */

    private String fullAddress;

    /**
     * 是否默认地址 0-否 1-是
     */
    private Integer isDefault;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-否 1-是
     */

    private Integer isDeleted;
}