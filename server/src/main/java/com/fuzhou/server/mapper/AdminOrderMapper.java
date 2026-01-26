package com.fuzhou.server.mapper;

import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 管理端订单Mapper接口
 */
@Mapper
public interface AdminOrderMapper {
    //分页查询订单
    List<OrderVO> pageQuery(OrderPageQueryDTO dto);
    //查询订单详情
    OrderVO getById(Long id);
    //更新订单
    void update(Order order);
}