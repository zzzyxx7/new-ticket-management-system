package com.fuzhou.server.service;

import com.fuzhou.pojo.entity.Sessions;

public interface SessionsService {
    /**
     * 根据ID查询场次信息（包含库存）
     * @param sessionId 场次ID
     * @return 场次信息
     */
    Sessions getById(Long sessionId);

    /**
     * 扣减库存
     * @param sessionId 场次ID
     * @param quantity 扣减数量
     * @return true=扣减成功，false=扣减失败（库存不足或并发冲突）
     */
    boolean decreaseStock(Long sessionId, Integer quantity);

    /**
     * 回退库存（订单取消时使用）
     * @param sessionId 场次ID
     * @param quantity 回退数量
     * @return true=回退成功，false=回退失败
     */
    boolean increaseStock(Long sessionId, Integer quantity);
}















