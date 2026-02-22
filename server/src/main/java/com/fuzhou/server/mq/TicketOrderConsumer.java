package com.fuzhou.server.mq;

import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.MailUtils;
import com.fuzhou.pojo.entity.User;
import com.fuzhou.server.config.RabbitMQConfig;
import com.fuzhou.server.mapper.UserLoginMapper;
import com.fuzhou.server.service.UserOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 抢票下单消息消费者：真正执行下单逻辑
 * 当 fuzhou.rabbitmq.enabled=false 时，不创建此消费者（适用于没有 RabbitMQ 的环境）
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "fuzhou.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class TicketOrderConsumer {

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private UserLoginMapper userLoginMapper;

    @Autowired
    private MailUtils mailUtils;

    @RabbitListener(
            queues = RabbitMQConfig.TICKET_ORDER_QUEUE,
            ackMode = "MANUAL",
            concurrency = "3-10"
    )
    public void onMessage(TicketOrderMessage message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        if (message == null) {
            log.warn("收到空的抢票消息，忽略");
            channel.basicAck(deliveryTag, false);
            return;
        }
        Long sessionId = message.getSessionId();
        Integer quantity = message.getQuantity();
        Long userId = message.getUserId();

        log.info("消费抢票消息：sessionId={}, quantity={}, userId={}", sessionId, quantity, userId);
        try {
            Result<String> result = userOrderService.createOrder(sessionId, quantity, userId);
            log.info("异步抢票处理结果：{}", result.getMsg());
            channel.basicAck(deliveryTag, false);
            // 抢票成功后给用户邮箱发通知
            if (result != null && result.getCode() != null && result.getCode() == 1 && userId != null) {
                try {
                    User user = userLoginMapper.selectUserById(userId);
                    if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                        String detail = result.getData() != null ? result.getData() : "请尽快登录大麦平台完成支付。";
                        boolean sent = mailUtils.sendTextEmail(
                                user.getEmail(),
                                "【大麦平台】抢票成功通知",
                                "尊敬的用户，您已抢票成功。" + detail + " 请尽快完成支付。"
                        );
                        if (sent) {
                            log.info("抢票成功通知邮件已发送至：{}", user.getEmail());
                        } else {
                            log.warn("抢票成功通知邮件发送失败，userId={}", userId);
                        }
                    }
                } catch (Exception mailEx) {
                    log.warn("发送抢票成功邮件异常，userId={}", userId, mailEx);
                }
            }
        } catch (BaseException e) {
            // 业务异常：记录日志，确认消息，不重试
            log.warn("异步抢票业务异常，sessionId={}, userId={}, msg={}", sessionId, userId, e.getMessage());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 系统异常：拒绝消息并丢入死信队列，便于排查
            log.error("异步抢票处理异常，将消息投递到死信队列，sessionId={}, userId={}", sessionId, userId, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}

