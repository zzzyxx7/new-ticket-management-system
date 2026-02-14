package com.fuzhou.pojo.vo;

import lombok.Data;
import java.util.List;

/**
 * 用户端演出详情VO
 * 包含场次信息、票档信息、库存信息、是否开票等
 */
@Data
public class ShowDetailVO {
    /**
     * 演出ID
     */
    private Long id;

    /**
     * 演出名称
     */
    private String title;

    /**
     * 演出封面图
     */
    private String image;

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
     * 场馆名称
     */
    private String venueName;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 完整地址
     */
    private String fullAddress;

    /**
     * 分类ID
     */
    private Long sortId;

    /**
     * 分类名称
     */
    private String sortName;

    /**
     * 参演演员列表
     */
    private List<ShowVO.ActorVO> actors;

    /**
     * 场次信息列表
     */
    private List<SessionsVO> sessions;

    /**
     * 是否有库存（用户端只返回布尔值，不返回具体数量）
     */
    private Boolean hasStock;

    /**
     * 是否已开票
     */
    private Boolean issued;
}















