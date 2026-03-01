package com.fuzhou.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminShowVO {
    private Long id;
    private String title;            // 演出名称
    private Long sortId;             // 分类ID
    private String sortName;         // 分类名称
    private String province;         // 省份
    private String city;             // 城市
    private String district;         // 区县
    private String venueName;        // 场馆名称
    private String detailAddress;    // 详细地址
    private String fullAddress;      // 完整地址
    private String image;            // 封面图
    private BigDecimal price;        // 价格
    private Integer stock;           // 剩余库存
    private Integer totalStock;      // 总库存
}
