package com.fuzhou.server.service.impl;

import com.fuzhou.pojo.dto.OrderModel;
import com.fuzhou.server.mapper.KOrderMapper;
import com.fuzhou.server.service.KOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KOrderServiceImpl implements KOrderService {
    @Autowired
    private KOrderMapper orderMapper;

    @Override
    public OrderModel getOrder(Integer id) {
        return orderMapper.getOrder(id);
    }

    @Override
    public boolean updateOrder(OrderModel orderModels) {
        return orderMapper.updateOrder(orderModels);
    }
}
