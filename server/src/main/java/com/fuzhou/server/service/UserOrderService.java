package com.fuzhou.server.service;

import com.fuzhou.common.result.Result;

public interface UserOrderService {

    /**
     * 用户端抢票/创建订单
     *
     * @param sessionId 场次ID
     * @param quantity  购买数量
     * @param userId    用户ID
     */
    Result<String> createOrder(Long sessionId, Integer quantity, Long userId);
}



