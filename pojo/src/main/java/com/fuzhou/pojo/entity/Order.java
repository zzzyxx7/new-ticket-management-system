package com.fuzhou.pojo.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;           // 订单编号
    private Long sessionId;           // 场次ID
    private Long userId;              // 用户ID
    private BigDecimal price;         // 订单金额
    private String alipayTradeNo;     // 支付宝交易号
    private Integer orderStatus;      // 订单状态：0-待支付 1-支付成功 2-支付失败 3-已取消 4-已退款
    private String subject;           // 订单标题
    private String body;              // 订单描述
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime payTime;    // 支付时间
}