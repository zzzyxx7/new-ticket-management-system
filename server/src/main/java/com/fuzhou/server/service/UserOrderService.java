package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.vo.OrderVO;

public interface UserOrderService {

    /**
     * 用户端抢票/创建订单
     *
     * @param sessionId 场次ID
     * @param quantity  购买数量
     * @param userId    用户ID
     */
    Result<String> createOrder(Long sessionId, Integer quantity, Long userId);

    /**
     * 用户端分页查询自己的订单
     */
    Result<PageResult<OrderVO>> pageQueryForUser(OrderPageQueryDTO dto, Long userId);

    /**
     * 用户端查询自己的订单详情
     */
    Result<OrderVO> getOrderDetail(Long id, Long userId);

    /**
     * 用户端取消订单
     */
    Result<String> cancelOrder(Long id, Long userId);
}



