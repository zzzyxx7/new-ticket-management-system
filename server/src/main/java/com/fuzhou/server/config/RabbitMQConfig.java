package com.fuzhou.server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 抢票相关的 RabbitMQ 基础配置：队列、交换机、路由键。
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 抢票下单队列名称
     */
    public static final String TICKET_ORDER_QUEUE = "ticket.order.queue";

    /**
     * 抢票下单交换机名称
     */
    public static final String TICKET_ORDER_EXCHANGE = "ticket.order.exchange";

    /**
     * 抢票下单路由键
     */
    public static final String TICKET_ORDER_ROUTING_KEY = "ticket.order.routing";

    /**
     * 抢票下单死信交换机名称
     */
    public static final String TICKET_ORDER_DLX_EXCHANGE = "ticket.order.dlx.exchange";

    /**
     * 抢票下单死信队列名称
     */
    public static final String TICKET_ORDER_DLX_QUEUE = "ticket.order.dlx.queue";

    /**
     * 抢票下单死信路由键
     */
    public static final String TICKET_ORDER_DLX_ROUTING_KEY = "ticket.order.dlx.routing";

    @Bean
    public Queue ticketOrderQueue() {
        // 持久化队列并绑定死信交换机，异常消息进入死信队列，便于排查
        return QueueBuilder.durable(TICKET_ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", TICKET_ORDER_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TICKET_ORDER_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange ticketOrderExchange() {
        return new DirectExchange(TICKET_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Binding ticketOrderBinding(Queue ticketOrderQueue, DirectExchange ticketOrderExchange) {
        return BindingBuilder.bind(ticketOrderQueue)
                .to(ticketOrderExchange)
                .with(TICKET_ORDER_ROUTING_KEY);
    }

    @Bean
    public Queue ticketOrderDlxQueue() {
        return QueueBuilder.durable(TICKET_ORDER_DLX_QUEUE).build();
    }

    @Bean
    public DirectExchange ticketOrderDlxExchange() {
        return new DirectExchange(TICKET_ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Binding ticketOrderDlxBinding(Queue ticketOrderDlxQueue, DirectExchange ticketOrderDlxExchange) {
        return BindingBuilder.bind(ticketOrderDlxQueue)
                .to(ticketOrderDlxExchange)
                .with(TICKET_ORDER_DLX_ROUTING_KEY);
    }

    /**
     * 使用 JSON 作为消息序列化格式，便于排查和扩展
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

