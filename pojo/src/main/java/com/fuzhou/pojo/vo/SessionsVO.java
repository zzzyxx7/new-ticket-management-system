package com.fuzhou.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 场次信息VO（用户端）
 */
@Data
public class SessionsVO {
    /**
     * 场次ID
     */
    private Long id;

    /**
     * 关联演出ID
     */
    private Long showId;

    /**
     * 场次开始时间
     */
    private LocalDateTime startTime;

    /**
     * 场次持续时间
     */
    private Duration duration;

    /**
     * 是否有库存（用户端只返回布尔值，不返回具体数量）
     */
    private Boolean hasStock;
}















