package com.fuzhou.server.mapper;

import com.fuzhou.pojo.entity.Sessions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SessionsMapper {

    // 根据演出ID查询场次列表（一个演出下的所有场次）
    List<Sessions> selectByShowId(Long showId);

    // 根据ID查询场次
    Sessions selectById(Long id);

    // 新增场次
    int insert(Sessions sessions);

    // 修改场次
    int update(Sessions sessions);

    // 删除场次
    int deleteById(Long id);

    // 扣减库存（使用乐观锁，基于业务字段）
    // @param sessionId 场次ID
    // @param quantity 扣减数量
    int decreaseStock(@Param("sessionId") Long sessionId,
                      @Param("quantity") Integer quantity);

    // 回退库存（订单取消时使用）
    // @param sessionId 场次ID
    // @param quantity 回退数量
    int increaseStock(@Param("sessionId") Long sessionId,
                      @Param("quantity") Integer quantity);
}

