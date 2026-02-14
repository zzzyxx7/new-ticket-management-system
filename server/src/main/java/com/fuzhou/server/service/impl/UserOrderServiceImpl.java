package com.fuzhou.server.service.impl;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.entity.Order;
import com.fuzhou.pojo.entity.Sessions;
import com.fuzhou.server.mapper.OrderMapper;
import com.fuzhou.server.service.SessionsService;
import com.fuzhou.server.service.UserOrderService;
import com.fuzhou.common.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private RedissonLockUtil redissonLockUtil;
    //抢票
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

            // 4. 计算价格（演示：用单价 1 * quantity，实际应从票价表获取）
            BigDecimal totalPrice = BigDecimal.valueOf(quantity);

            // 5. 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setSessionId(sessionId);
            order.setUserId(currentUserId);
            order.setPrice(totalPrice);
            order.setOrderStatus(0); // 0-待支付
            order.setSubject("演出票订单");
            order.setBody("sessionId=" + sessionId);
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
}


