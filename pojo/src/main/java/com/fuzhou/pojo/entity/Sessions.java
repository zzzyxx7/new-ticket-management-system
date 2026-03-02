package com.fuzhou.pojo.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Sessions {
    private Long id;
    private Long showId;             // 演出ID
    private BigDecimal price;        // 单价
    private LocalDateTime startTime; // 开始时间
    private String duration;         // 持续时长（HH:mm:ss）
    private Integer stock;           // 剩余库存
    private Integer totalStock;      // 总库存
}
