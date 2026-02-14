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
    private Integer quantity;         // 购买数量
    private BigDecimal totalPrice;    // 总价格 = 单价 × 数量
    private String alipayTradeNo;     // 支付宝交易号
    private Integer orderStatus;      // 订单状态 0-未完成订单 1-以完成订单 2-退款订单 3-取消订单
    private Integer paymentStatus;    // 支付状态：0-未支付 1-已支付
    private String subject;           // 订单标题
    private String body;              // 订单描述
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime payTime;    // 支付时间
}