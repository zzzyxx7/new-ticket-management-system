package com.fuzhou.pojo.dto;

import lombok.Data;

/**
 * 地址添加/修改 DTO（参考 ticket-system）
 */
@Data
public class AddressDTO {
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
    private Boolean isDefault;
}
