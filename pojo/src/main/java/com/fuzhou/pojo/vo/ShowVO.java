package com.fuzhou.pojo.vo;

import com.fuzhou.pojo.entity.Actor;
import lombok.Data;

import java.util.List;

/**
 * 节目展示VO（前端页面展示专用）
 * 仅包含前端需要的核心字段，屏蔽数据库层冗余信息
 */
@Data
public class ShowVO {
    /**
     * 节目ID（前端展示/跳转用）
     */
    private Long id;

    /**
     * 节目名称（核心展示字段）
     */
    private String title;

    /**
     * 节目封面图（show表新增的image字段）
     */
    private String image;

    /**
     * 演出城市（简化展示，无需区县/省份冗余）
     */
    private String city;

    /**
     * 演出场馆名称（核心展示字段）
     */
    private String venueName;

    /**
     * 节目分类名称（直接展示分类名，而非分类ID，前端更友好）
     */
    private String sortName;

    /**
     * 参演演员列表（关联展示，仅保留演员核心信息）
     */
    private List<ActorVO> actors;

    /**
     * 是否有库存（用户端只返回布尔值，不返回具体数量）
     */
    private Boolean hasStock;

    // 嵌套VO：演员展示专用（仅保留前端需要的字段，避免返回冗余的人气值等）
    @Data
    public static class ActorVO {
        /**
         * 演员ID
         */
        private Long id;

        private Long showId;
        /**
         * 演员姓名
         */
        private String name;

    }
}