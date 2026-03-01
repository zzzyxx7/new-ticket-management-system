package com.fuzhou.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAddress {
    private Long id;
    private Long userId;             // 用户ID
    private String province;         // 省份
    private String city;             // 城市
    private String district;         // 区县
    private String detailedAddress;  // 详细地址
    private String fullAddress;      // 完整地址
    private Integer isDefault;       // 是否默认 0-否 1-是
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间
    private Integer isDeleted;       // 是否删除 0-否 1-是
}
