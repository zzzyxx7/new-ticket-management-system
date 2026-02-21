package com.fuzhou.server.mq;

import java.io.Serializable;

/**
 * 抢票下单消息体
 */
public class TicketOrderMessage implements Serializable {

    private Long sessionId;

    private Integer quantity;

    private Long userId;

    public TicketOrderMessage() {
    }

    public TicketOrderMessage(Long sessionId, Integer quantity, Long userId) {
        this.sessionId = sessionId;
        this.quantity = quantity;
        this.userId = userId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}




