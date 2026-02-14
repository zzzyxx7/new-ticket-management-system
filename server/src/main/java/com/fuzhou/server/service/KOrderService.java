package com.fuzhou.server.service;

import com.fuzhou.pojo.dto.OrderModel;

public interface KOrderService {
    OrderModel getOrder(Integer id);

    boolean updateOrder(OrderModel orderModels);
}
