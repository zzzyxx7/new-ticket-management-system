package com.fuzhou.server.service.impl;

import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.vo.OrderVO;
import com.fuzhou.server.mapper.AdminOrderMapper;
import com.fuzhou.server.service.AdminOrderService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {
    @Autowired
    private AdminOrderMapper adminOrderMapper;

    @Override
    public PageResult pageQuery(OrderPageQueryDTO dto) {
        // 处理分页参数
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }

        // 使用PageHelper分页
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 查询订单列表
        List<OrderVO> list = adminOrderMapper.pageQuery(dto);

        // 封装分页结果
        Page<OrderVO> page = (Page<OrderVO>) list;
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderVO getById(Long id) {
        if (id == null) {
            throw new BaseException("订单ID不能为空");
        }
        OrderVO orderVO = adminOrderMapper.getById(id);
        if (orderVO == null) {
            throw new BaseException("订单不存在");
        }
        return orderVO;
    }

    @Override
    @Transactional
    public void update(Order order) {
        if (order == null || order.getId() == null) {
            throw new BaseException("订单信息或订单ID不能为空");
        }

        // 先查询订单是否存在
        OrderVO existingOrder = adminOrderMapper.getById(order.getId());
        if (existingOrder == null) {
            throw new BaseException("订单不存在");
        }

        // 更新订单
        adminOrderMapper.update(order);
        log.info("管理员更新订单，订单ID：{}", order.getId());
    }
}