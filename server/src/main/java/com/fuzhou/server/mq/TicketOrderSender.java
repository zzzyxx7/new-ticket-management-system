package com.fuzhou.server.mq;

import com.fuzhou.server.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 抢票下单消息发送端
 */
@Component
@Slf4j
public class TicketOrderSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(Long sessionId, Integer quantity, Long userId) {
        TicketOrderMessage message = new TicketOrderMessage(sessionId, quantity, userId);
        log.info("发送抢票消息到 MQ，sessionId={}, quantity={}, userId={}", sessionId, quantity, userId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TICKET_ORDER_EXCHANGE,
                RabbitMQConfig.TICKET_ORDER_ROUTING_KEY,
                message
        );
    }
}




