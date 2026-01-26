package com.fuzhou.pojo.vo;

import lombok.Data;

// 这个只是暂定的，后面会进行修改
@Data
public class AddressVO {
    private Long id;
    private String fullAddress;
    private Integer isDefault;
}
