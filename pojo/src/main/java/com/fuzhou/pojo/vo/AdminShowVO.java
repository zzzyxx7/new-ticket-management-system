package com.fuzhou.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端演出详情VO
 * 包含演出的所有字段以及关联的分类名称
 */
@Data
public class AdminShowVO {
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 节目名称
     */
    private String title;

    /**
     * 关联分类 ID
     */
    private Long sortId;

    /**
     * 分类名称（关联查询）
     */
    private String sortName;

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
     * 演出场馆名称
     */
    private String venueName;

    /**
     * 场馆详细地址（门牌号等）
     */
    private String detailAddress;

    /**
     * 完整地址（冗余字段）
     */
    private String fullAddress;

    /**
     * 节目封面图
     */
    private String image;

    /**
     * 最低票价（取该演出所有场次的最低价）
     */
    private BigDecimal price;

    /**
     * 剩余库存（该演出下所有场次的剩余库存总和）
     */
    private Integer stock;

    /**
     * 总库存（该演出下所有场次的初始库存总和）
     * 用于计算已售数量：已售 = totalStock - stock
     */
    private Integer totalStock;
}


