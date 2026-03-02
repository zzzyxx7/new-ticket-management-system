package com.fuzhou.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SessionsVO {
    private Long id;
    private Long showId;             // 演出ID
    private BigDecimal price;        // 单价
    private LocalDateTime startTime; // 开始时间
    private String duration;         // 持续时长（HH:mm:ss）
    private Boolean hasStock;        // 是否有库存
}
