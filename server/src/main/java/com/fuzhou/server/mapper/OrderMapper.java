package com.fuzhou.server.mapper;

import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    Order getById(Long id);

    /**
     * 用户端分页查询自己的订单
     */
    List<OrderVO> pageQueryByUser(OrderPageQueryDTO dto);

    /**
     * 根据订单ID和用户ID查询订单（防止越权）
     */
    OrderVO getByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 更新订单状态（用户端取消订单使用）
     */
    int updateStatus(@Param("id") Long id,
                     @Param("userId") Long userId,
                     @Param("orderStatus") Integer orderStatus,
                     @Param("updateTime") LocalDateTime updateTime);
}



