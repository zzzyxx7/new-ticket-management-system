package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.vo.OrderVO;


public interface AdminOrderService {
    // 管理端分页查询订单列表
    PageResult pageQuery(OrderPageQueryDTO dto);
    //管理端查询订单详情
    OrderVO getById(Long id);
    //管理端修改订单信息
    void update(Order order);
}