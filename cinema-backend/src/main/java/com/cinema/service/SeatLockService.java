package com.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 座位分布式锁: 锁座前置到 Redis, 键为 lock:seat:{scheduleId}:{seat}。
 * 下单成功后锁保留至租期结束(10 分钟, 与待支付超时时间一致), 由取消/超时关单流程提前释放。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatLockService {

    /** 锁租期(分钟), 与未支付订单超时关单时间保持一致 */
    public static final long LEASE_MINUTES = 10;

    private static final String KEY_PREFIX = "lock:seat:";

    private final RedissonClient redissonClient;

    public String key(Long scheduleId, String seat) {
        return KEY_PREFIX + scheduleId + ":" + seat;
    }

    /**
     * 依次为座位加锁, 不等待(waitTime=0), 抢不到立即返回。
     * 调用方需按座位号排序后传入, 保证多座位订单的加锁顺序一致, 避免交叉死锁。
     *
     * @return null 表示全部加锁成功; 否则返回被抢占的座位, 且本次已获取的锁已释放
     */
    public String lockSeats(Long scheduleId, Collection<String> seats) {
        List<RLock> acquired = new ArrayList<>(seats.size());
        for (String seat : seats) {
            RLock lock = redissonClient.getLock(key(scheduleId, seat));
            boolean locked;
            try {
                locked = lock.tryLock(0, LEASE_MINUTES, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                locked = false;
            }
            if (!locked) {
                acquired.forEach(this::unlockQuietly);
                return seat;
            }
            acquired.add(lock);
        }
        return null;
    }

    /**
     * 释放订单占用的座位锁。取消/超时关单通常发生在其它线程, 锁并非当前线程持有, 因此需要强制释放。
     */
    public void unlockSeats(Long scheduleId, String seats) {
        if (seats == null || seats.isBlank()) {
            return;
        }
        for (String seat : seats.split(",")) {
            String s = seat.trim();
            if (s.isEmpty()) {
                continue;
            }
            RLock lock = redissonClient.getLock(key(scheduleId, s));
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                } else if (lock.isLocked()) {
                    lock.forceUnlock();
                }
            } catch (Exception e) {
                log.warn("释放座位锁失败: scheduleId={}, seat={}, msg={}", scheduleId, s, e.getMessage());
            }
        }
    }

    private void unlockQuietly(RLock lock) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("回滚座位锁失败: key={}, msg={}", lock.getName(), e.getMessage());
        }
    }
}
