package com.fuzhou.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class ShowDetailVO {
    private Long id;
    private String title;            // 演出名称
    private String image;            // 封面图
    private String province;        // 省份
    private String city;             // 城市
    private String district;         // 区县
    private String venueName;        // 场馆名称
    private String detailAddress;    // 详细地址
    private String fullAddress;      // 完整地址
    private Long sortId;             // 分类ID
    private String sortName;         // 分类名称
    private List<ShowVO.ActorVO> actors;  // 演员列表
    private List<SessionsVO> sessions;    // 场次列表
    private Boolean hasStock;        // 是否有库存
    private Boolean issued;          // 是否已开票
}
