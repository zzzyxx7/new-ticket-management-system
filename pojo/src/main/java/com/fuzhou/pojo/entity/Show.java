package com.fuzhou.pojo.entity;
import lombok.Data;

@Data
public class Show {
    private Long id;
    private String title;             // 节目名称
    private Long sortId;              // 分类ID
    private String province;         // 省份
    private String city;             // 城市
    private String district;         // 区县
    private String venueName;        // 场馆名称
    private String detailAddress;    // 详细地址
    private String fullAddress;      // 完整地址
    private String image;            // 封面图
}
