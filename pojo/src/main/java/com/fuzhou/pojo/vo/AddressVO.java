package com.fuzhou.pojo.vo;

import lombok.Data;

/**
 * 地址展示 VO（含省市区、详细地址、是否默认）
 */
@Data
public class AddressVO {
    private Long id;
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
    private String fullAddress;
    private Integer isDefault;
}
