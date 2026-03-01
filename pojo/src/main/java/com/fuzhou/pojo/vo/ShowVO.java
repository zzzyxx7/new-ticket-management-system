package com.fuzhou.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShowVO {
    private Long id;
    private String title;            // 演出名称
    private String image;            // 封面图
    private BigDecimal price;       // 价格
    private String city;            // 城市
    private String venueName;       // 场馆名称
    private String sortName;        // 分类名称
    private List<ActorVO> actors;   // 演员列表
    private Boolean hasStock;       // 是否有库存

    @Data
    public static class ActorVO {
        private Long id;
        private Long showId;         // 演出ID
        private String name;        // 演员姓名
    }
}
