package com.fuzhou.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    // 订单基本信息
    private Long id;
    private String orderNo;           // 订单编号
    private Long sessionId;           // 场次ID
    private Long userId;              // 用户ID
    private Integer quantity;         // 购买数量
    private BigDecimal price;         // 订单金额
    private BigDecimal totalPrice;    // 总金额
    private String alipayTradeNo;     // 支付宝交易号
    private Integer orderStatus;      // 订单状态
    private Integer paymentStatus;    // 支付状态
    private String subject;           // 订单标题
    private String body;              // 订单描述
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payTime;

    // 关联的用户信息（从user表查询）
    private String userName;          // 用户姓名
    private String userAccount;       // 用户账号
    private String userEmail;         // 用户邮箱

    // 关联的场次信息（从sessions表查询）
    private LocalDateTime sessionStartTime;  // 场次开始时间
    private Long showId;                     // 演出ID

    // 关联的演出信息（从show表查询）
    private String showTitle;         // 演出名称
    private String venueName;         // 场馆名称
    private String city;              // 城市
}