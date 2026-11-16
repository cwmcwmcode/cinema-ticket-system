package com.cinema.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cinema.entity.Order;
import com.cinema.mapper.OrderMapper;
import com.cinema.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 未支付订单超时关单: 每 30 秒扫描一次, 超过 10 分钟未支付的订单置为已取消, 并释放对应座位锁。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    /** 待支付超时时间(分钟), 与座位锁租期一致 */
    private static final int TIMEOUT_MINUTES = 10;

    private final OrderMapper orderMapper;
    private final SeatLockService seatLockService;

    @Scheduled(fixedDelay = 30_000L)
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<Order> timeoutOrders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, deadline));
        if (timeoutOrders.isEmpty()) {
            return;
        }
        for (Order order : timeoutOrders) {
            // 条件更新: 只关仍未支付的单, 避免覆盖并发支付/取消的结果
            int rows = orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                    .eq(Order::getId, order.getId())
                    .eq(Order::getStatus, 0)
                    .set(Order::getStatus, 2));
            if (rows == 0) {
                continue;
            }
            seatLockService.unlockSeats(order.getScheduleId(), order.getSeats());
            log.info("超时关单成功, 已释放座位锁: orderId={}, scheduleId={}, seats={}",
                    order.getId(), order.getScheduleId(), order.getSeats());
        }
    }
}
