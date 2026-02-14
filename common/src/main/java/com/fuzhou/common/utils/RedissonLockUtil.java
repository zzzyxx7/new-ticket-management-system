package com.fuzhou.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockUtil {

    private final RedissonClient redissonClient;

    private static final String LOCK_KEY_PREFIX = "lock:";

    public RLock getLock(String lockKey) {
        return redissonClient.getLock(LOCK_KEY_PREFIX + lockKey);
    }

    public boolean tryLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds) {
        RLock lock = getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS);
            if (acquired) {
                log.debug("获取锁成功, lockKey: {}, waitTime: {}s, leaseTime: {}s",
                        lockKey, waitTimeSeconds, leaseTimeSeconds);
            } else {
                log.warn("获取锁超时, lockKey: {}, waitTime: {}s", lockKey, waitTimeSeconds);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断, lockKey: {}", lockKey, e);
            return false;
        } catch (Exception e) {
            log.error("获取锁失败, lockKey: {}", lockKey, e);
            return false;
        }
    }

    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
                log.debug("释放锁成功, lockKey: {}", lockKey);
            } catch (Exception e) {
                log.error("释放锁失败, lockKey: {}", lockKey, e);
            }
        } else {
            log.warn("尝试释放锁但当前线程未持有该锁, lockKey: {}", lockKey);
        }
    }

    public <T> T executeWithLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> supplier) {
        RLock lock = getLock(lockKey);
        try {
            if (lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS)) {
                try {
                    log.debug("执行带锁操作, lockKey: {}, waitTime: {}s, leaseTime: {}s",
                            lockKey, waitTimeSeconds, leaseTimeSeconds);
                    return supplier.get();
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("释放锁成功, lockKey: {}", lockKey);
                    }
                }
            } else {
                log.warn("获取锁超时, lockKey: {}, waitTime: {}s", lockKey, waitTimeSeconds);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断, lockKey: {}", lockKey, e);
            throw new RuntimeException("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("执行带锁操作异常, lockKey: {}", lockKey, e);
            throw new RuntimeException("执行操作失败: " + e.getMessage(), e);
        }
    }
}



