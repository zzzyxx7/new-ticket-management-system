package com.fuzhou.server.mapper;

import com.fuzhou.pojo.dto.OrderModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KOrderMapper {
    OrderModel getOrder(Integer id);

    boolean updateOrder(OrderModel orderModels);
}
