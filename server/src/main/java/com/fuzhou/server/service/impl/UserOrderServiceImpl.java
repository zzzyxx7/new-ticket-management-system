package com.fuzhou.server.service.impl;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.OrderPageQueryDTO;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.entity.Sessions;
import com.fuzhou.pojo.vo.OrderVO;
import com.fuzhou.server.mapper.OrderMapper;
import com.fuzhou.server.mapper.ShowMapper;
import com.fuzhou.server.service.SessionsService;
import com.fuzhou.server.service.UserOrderService;
import com.fuzhou.common.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.github.pagehelper.PageHelper;

/**
 * 用户端抢票/下单 Service，使用 Redisson 分布式锁保护库存扣减。
 */
@Service
@Slf4j
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    private SessionsService sessionsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShowMapper showMapper;

    @Autowired
    private RedissonLockUtil redissonLockUtil;

    // 抢票 / 创建订单
    @Override
    @Transactional
    public Result<String> createOrder(Long sessionId, Integer quantity, Long userId) {
        if (sessionId == null || quantity == null || quantity <= 0) {
            return Result.error("参数错误：场次ID和数量不能为空且必须大于0");
        }
        Long resolvedUserId = userId;
        if (resolvedUserId == null) {
            resolvedUserId = BaseContext.getCurrentId();
        }
        if (resolvedUserId == null) {
            throw new BaseException("用户未登录");
        }
        final Long currentUserId = resolvedUserId;

        String lockKey = "order:create:session:" + sessionId;

        return redissonLockUtil.executeWithLock(lockKey, 1, 10, () -> {
            // 1. 查询场次信息（在锁内，避免并发修改）
            Sessions session = sessionsService.getById(sessionId);
            if (session == null) {
                throw new BaseException("场次不存在");
            }

            // 2. 先检查一次库存
            Integer stock = session.getStock();
            if (stock == null || stock < quantity) {
                return Result.error("库存不足，抢票失败");
            }

            // 3. 使用乐观锁扣减库存（内部再次检查 stock >= quantity）
            boolean success = sessionsService.decreaseStock(sessionId, quantity);
            if (!success) {
                return Result.error("库存不足，抢票失败");
            }

            // 4. 价格：从场次取单价，总价 = 单价 × 数量
            BigDecimal unitPrice = session.getPrice() != null && session.getPrice().compareTo(BigDecimal.ZERO) > 0
                    ? session.getPrice()
                    : BigDecimal.ONE;
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // 5. 订单标题与描述：与所购场次一致，便于支付页与订单展示
            String showTitle = showMapper.getTitleById(session.getShowId());
            if (showTitle == null || showTitle.isEmpty()) {
                showTitle = "演出票订单";
            }
            String sessionTime = session.getStartTime() == null
                    ? ""
                    : session.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String bodyDesc = String.format("场次时间：%s，%d张", sessionTime, quantity);

            // 6. 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setSessionId(sessionId);
            order.setUserId(currentUserId);
            order.setQuantity(quantity);
            order.setPrice(unitPrice);
            order.setTotalPrice(totalPrice);
            order.setOrderStatus(0); // 0-待支付
            order.setPaymentStatus(0);
            order.setSubject(showTitle);
            order.setBody(bodyDesc);
            LocalDateTime now = LocalDateTime.now();
            order.setCreateTime(now);
            order.setUpdateTime(now);

            orderMapper.insert(order);

            log.info("用户下单成功，orderId={}, userId={}, sessionId={}, quantity={}",
                    order.getId(), currentUserId, sessionId, quantity);

            return Result.success("抢票成功，订单ID：" + order.getId());
        });
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    @Override
    public Result<PageResult<OrderVO>> pageQueryForUser(OrderPageQueryDTO dto, Long userId) {
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        dto.setUserId(userId);
        // 处理分页参数
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        List<OrderVO> list = orderMapper.pageQueryByUser(dto);
        Page<OrderVO> page = (Page<OrderVO>) list;
        return Result.success(new PageResult<>(page.getTotal(), page.getResult()));
    }

    @Override
    public Result<OrderVO> getOrderDetail(Long id, Long userId) {
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        if (id == null) {
            return Result.error("订单ID不能为空");
        }
        OrderVO orderVO = orderMapper.getByIdAndUser(id, userId);
        if (orderVO == null) {
            return Result.error("订单不存在");
        }
        return Result.success(orderVO);
    }

    @Override
    @Transactional
    public Result<String> cancelOrder(Long id, Long userId) {
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        if (id == null) {
            return Result.error("订单ID不能为空");
        }
        OrderVO orderVO = orderMapper.getByIdAndUser(id, userId);
        if (orderVO == null) {
            return Result.error("订单不存在");
        }

        String lockKey = "order:cancel:session:" + orderVO.getSessionId();
        return redissonLockUtil.executeWithLock(lockKey, 1, 10, () -> {
            OrderVO current = orderMapper.getByIdAndUser(id, userId);
            if (current == null) {
                return Result.error("订单不存在");
            }
            if (current.getOrderStatus() == null || current.getOrderStatus() != 0) {
                return Result.error("只能取消未完成的订单");
            }
            if (current.getPayTime() != null || (current.getPaymentStatus() != null && current.getPaymentStatus() == 1)) {
                return Result.error("已支付订单不能取消");
            }
            if (current.getQuantity() == null || current.getQuantity() <= 0) {
                throw new BaseException("订单数量异常，无法回退库存");
            }

            int rows = orderMapper.updateStatus(id, userId, 3, LocalDateTime.now());
            if (rows <= 0) {
                return Result.error("取消订单失败");
            }

            boolean stockBack = sessionsService.increaseStock(current.getSessionId(), current.getQuantity());
            if (!stockBack) {
                throw new BaseException("库存回退失败");
            }

            return Result.success("取消订单成功");
        });
    }
}


