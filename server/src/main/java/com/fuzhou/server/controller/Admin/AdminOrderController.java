package com.fuzhou.server.controller.Admin;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.vo.OrderVO;
import com.fuzhou.server.service.AdminOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Slf4j
public class AdminOrderController {
    @Autowired
    private AdminOrderService adminOrderService;

    //分页查询订单信息
    @GetMapping
    public Result<PageResult> pageQuery(OrderPageQueryDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]分页查询订单列表，条件：{}", adminId, dto);
        PageResult pageResult = adminOrderService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @GetMapping("/detail")
    public Result<OrderVO> getById(Long id) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]查询订单详情，订单ID：{}", adminId, id);
        OrderVO orderVO = adminOrderService.getById(id);
        return Result.success(orderVO);
    }

    @PutMapping
    public Result update(@RequestBody Order order) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]修改订单信息，订单信息：{}", adminId, order);
        adminOrderService.update(order);
        return Result.success("修改成功");
    }
}