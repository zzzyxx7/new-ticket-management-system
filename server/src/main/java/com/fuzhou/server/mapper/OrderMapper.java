package com.fuzhou.server.mapper;

import com.fuzhou.pojo.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    Order getById(Long id);
}



