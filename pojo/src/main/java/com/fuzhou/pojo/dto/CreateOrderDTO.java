package com.fuzhou.pojo.dto;

import lombok.Data;

/**
 * 用户下单 / 抢票请求参数
 */
@Data
public class CreateOrderDTO {

    /**
     * 场次ID
     */
    private Long sessionId;

    /**
     * 购买数量
     */
    private Integer quantity;
}



