package com.fuzhou.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

@Data
public class SessionsVO {
    private Long id;
    private Long showId;             // 演出ID
    private BigDecimal price;        // 单价
    private LocalDateTime startTime; // 开始时间
    private Duration duration;       // 持续时间
    private Boolean hasStock;        // 是否有库存
}
