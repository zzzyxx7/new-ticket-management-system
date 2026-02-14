package com.fuzhou.pojo.dto;

import lombok.Data;

@Data
public class OrderModel {
    private Integer id;
    private Long userId;
    private String subject;
    private Integer orderStatus;
    private Integer paymentStatus;
    private Integer sessionId;
    private Double totalPrice;
}
