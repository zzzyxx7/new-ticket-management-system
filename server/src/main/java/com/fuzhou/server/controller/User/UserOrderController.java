package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.vo.OrderVO;
import com.fuzhou.server.service.UserOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/order")
@Slf4j
public class UserOrderController {

    @Autowired
    private UserOrderService userOrderService;

    /**
     * 用户端分页查询订单列表
     */
    @GetMapping
    public Result<PageResult<OrderVO>> page(OrderPageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户[{}]分页查询订单列表，条件：{}", userId, dto);
        return userOrderService.pageQueryForUser(dto, userId);
    }

    /**
     * 用户端获取订单详情
     */
    @GetMapping("/detail")
    public Result<OrderVO> detail(@RequestParam Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户[{}]查询订单详情，订单ID：{}", userId, id);
        return userOrderService.getOrderDetail(id, userId);
    }

    /**
     * 用户端取消订单
     */
    @PutMapping("/cancel")
    public Result<String> cancel(@RequestParam Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户[{}]取消订单，订单ID：{}", userId, id);
        return userOrderService.cancelOrder(id, userId);
    }
}






