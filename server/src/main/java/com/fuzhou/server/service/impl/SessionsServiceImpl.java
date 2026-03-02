package com.fuzhou.server.service.impl;

import com.fuzhou.common.exception.BaseException;
import com.fuzhou.pojo.entity.Sessions;
import com.fuzhou.server.mapper.SessionsMapper;
import com.fuzhou.server.service.SessionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SessionsServiceImpl implements SessionsService {

    @Autowired
    private SessionsMapper sessionsMapper;

    @Override
    public List<Sessions> listByShowId(Long showId) {
        if (showId == null) {
            throw new BaseException("演出ID不能为空");
        }
        return sessionsMapper.selectByShowId(showId);
    }

    @Override
    public Sessions getById(Long sessionId) {
        if (sessionId == null) {
            throw new BaseException("场次ID不能为空");
        }
        return sessionsMapper.selectById(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSession(Sessions sessions) {
        if (sessions == null || sessions.getShowId() == null) {
            throw new BaseException("场次信息或演出ID不能为空");
        }
        if (sessions.getTotalStock() == null || sessions.getTotalStock() < 0) {
            throw new BaseException("总库存不能为空且不能为负数");
        }
        if (sessions.getStock() == null) {
            sessions.setStock(sessions.getTotalStock());
        }
        if (sessions.getStock() < 0 || sessions.getStock() > sessions.getTotalStock()) {
            throw new BaseException("剩余库存必须在 0 到总库存之间");
        }
        sessionsMapper.insert(sessions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSession(Sessions sessions) {
        if (sessions == null || sessions.getId() == null) {
            throw new BaseException("场次信息或场次ID不能为空");
        }

        Sessions dbSession = sessionsMapper.selectById(sessions.getId());
        if (dbSession == null) {
            throw new BaseException("场次不存在");
        }

        Integer newTotalStock = sessions.getTotalStock() != null ? sessions.getTotalStock() : dbSession.getTotalStock();
        Integer newStock = sessions.getStock() != null ? sessions.getStock() : dbSession.getStock();

        if (newTotalStock == null || newTotalStock < 0) {
            throw new BaseException("总库存不能为空且不能为负数");
        }
        if (newStock == null || newStock < 0 || newStock > newTotalStock) {
            throw new BaseException("剩余库存必须在 0 到总库存之间");
        }

        sessions.setTotalStock(newTotalStock);
        sessions.setStock(newStock);

        sessionsMapper.update(sessions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long id) {
        if (id == null) {
            throw new BaseException("场次ID不能为空");
        }
        Sessions dbSession = sessionsMapper.selectById(id);
        if (dbSession == null) {
            throw new BaseException("场次不存在");
        }
        sessionsMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long sessionId, Integer quantity) {
        if (sessionId == null || quantity == null || quantity <= 0) {
            throw new BaseException("参数错误：场次ID和扣减数量不能为空且必须大于0");
        }

        // 1. 先查询场次是否存在
        Sessions session = sessionsMapper.selectById(sessionId);
        if (session == null) {
            throw new BaseException("场次不存在：sessionId=" + sessionId);
        }

        // 2. 检查库存是否充足（双重检查）
        if (session.getStock() == null || session.getStock() < quantity) {
            log.warn("库存不足：sessionId={}, 当前库存={}, 需要数量={}",
                sessionId, session.getStock(), quantity);
            return false;
        }

        // 3. 使用乐观锁扣减库存（WHERE stock >= #{quantity} 保证原子性）
        int updateCount = sessionsMapper.decreaseStock(sessionId, quantity);

        // 4. 判断更新是否成功
        if (updateCount > 0) {
            log.info("库存扣减成功：sessionId={}, 扣减数量={}", sessionId, quantity);
            return true;
        } else {
            // 更新失败，说明库存不足（可能被其他线程抢完）
            log.warn("库存扣减失败（库存不足或被其他线程抢完）：sessionId={}, quantity={}",
                sessionId, quantity);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long sessionId, Integer quantity) {
        if (sessionId == null || quantity == null || quantity <= 0) {
            throw new BaseException("参数错误：场次ID和回退数量不能为空且必须大于0");
        }

        try {
            int updateCount = sessionsMapper.increaseStock(sessionId, quantity);
            if (updateCount > 0) {
                log.info("库存回退成功：sessionId={}, 回退数量={}", sessionId, quantity);
                return true;
            } else {
                log.warn("库存回退失败：sessionId={}, quantity={}", sessionId, quantity);
                return false;
            }
        } catch (Exception e) {
            log.error("库存回退异常：sessionId={}, quantity={}", sessionId, quantity, e);
            throw new BaseException("库存回退失败：" + e.getMessage());
        }
    }
}

