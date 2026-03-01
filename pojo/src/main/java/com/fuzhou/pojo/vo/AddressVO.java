package com.fuzhou.pojo.vo;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private String province;         // 省份
    private String city;             // 城市
    private String district;         // 区县
    private String detailedAddress;  // 详细地址
    private String fullAddress;      // 完整地址
    private Integer isDefault;       // 是否默认 0-否 1-是
}
