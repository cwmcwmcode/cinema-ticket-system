package com.cinema.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cinema.common.BusinessException;
import com.cinema.dto.OrderCreateDTO;
import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Order;
import com.cinema.entity.Schedule;
import com.cinema.mapper.HallMapper;
import com.cinema.mapper.MovieMapper;
import com.cinema.mapper.OrderMapper;
import com.cinema.mapper.ScheduleMapper;
import com.cinema.mapper.SysUserMapper;
import com.cinema.service.OrderService;
import com.cinema.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Pattern SEAT_PATTERN = Pattern.compile("^(\\d+)排(\\d+)座$");
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final ScheduleMapper scheduleMapper;
    private final MovieMapper movieMapper;
    private final HallMapper hallMapper;
    private final SysUserMapper userMapper;
    private final SeatLockService seatLockService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 选座下单: 先抢 Redis 座位锁, 抢不到直接返回, 不触碰数据库; 抢到后再进 MySQL 事务检查并落单。
     * 这里不加 @Transactional, 否则事务会在加锁前开启, 锁便失去串行意义。
     */
    @Override
    public Order create(OrderCreateDTO dto) {
        if (dto.getUserId() == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getScheduleId() == null) {
            throw new BusinessException("场次不能为空");
        }
        // 座位格式为纯字符串校验, 不查库
        List<String> seats = normalizeSeats(dto.getSeats());

        // 1. 锁座前置: 锁键 lock:seat:{scheduleId}:{seat}, 等待 0 秒, 租期 10 分钟
        String conflict = seatLockService.lockSeats(dto.getScheduleId(), seats);
        if (conflict != null) {
            throw new BusinessException("该座位已被锁定或抢购");
        }

        // 2. 锁定成功后才开启数据库事务: 校验场次/座位并生成订单
        try {
            return transactionTemplate.execute(status -> doCreate(dto, seats));
        } catch (RuntimeException e) {
            // 落单失败立即释放锁, 避免座位被无效占用到租期结束
            seatLockService.unlockSeats(dto.getScheduleId(), String.join(",", seats));
            throw e;
        }
    }

    private Order doCreate(OrderCreateDTO dto, List<String> seats) {
        if (userMapper.selectById(dto.getUserId()) == null) {
            throw new BusinessException("用户不存在");
        }
        Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("场次不存在");
        }
        Hall hall = hallMapper.selectById(schedule.getHallId());
        if (hall == null) {
            throw new BusinessException("影厅不存在");
        }

        for (String seat : seats) {
            Matcher m = SEAT_PATTERN.matcher(seat);
            if (!m.matches()) {
                throw new BusinessException("座位格式错误: " + seat + ", 应为 1排2座");
            }
            int row = Integer.parseInt(m.group(1));
            int col = Integer.parseInt(m.group(2));
            if (row < 1 || row > hall.getRowCount() || col < 1 || col > hall.getColCount()) {
                throw new BusinessException("座位超出影厅范围: " + seat);
            }
        }

        Set<String> occupied = new HashSet<>(occupiedSeats(dto.getScheduleId()));
        List<String> conflict = seats.stream().filter(occupied::contains).toList();
        if (!conflict.isEmpty()) {
            throw new BusinessException("座位已被选: " + String.join(",", conflict));
        }

        BigDecimal price = schedule.getPrice() == null ? BigDecimal.ZERO : schedule.getPrice();
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(dto.getUserId());
        order.setScheduleId(dto.getScheduleId());
        order.setSeats(String.join(",", seats));
        order.setTotalPrice(price.multiply(BigDecimal.valueOf(seats.size())));
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return fill(order);
    }

    private List<String> normalizeSeats(List<String> rawSeats) {
        if (rawSeats == null || rawSeats.isEmpty()) {
            throw new BusinessException("请选择座位");
        }
        // 排序后再加锁, 保证同一批座位在多请求间加锁顺序一致
        List<String> seats = rawSeats.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
        if (seats.isEmpty()) {
            throw new BusinessException("请选择座位");
        }
        for (String seat : seats) {
            if (!SEAT_PATTERN.matcher(seat).matches()) {
                throw new BusinessException("座位格式错误: " + seat + ", 应为 1排2座");
            }
        }
        return seats;
    }

    @Override
    public List<Order> listByUser(Long userId) {
        List<Order> list = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime));
        list.forEach(this::fill);
        return list;
    }

    @Override
    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return fill(order);
    }

    @Override
    public Order pay(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 条件更新, 避免支付与超时关单并发时覆盖状态
        int rows = orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 0)
                .set(Order::getStatus, 1));
        if (rows == 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        order.setStatus(1);
        return fill(order);
    }

    @Override
    public Order cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        int rows = orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                .eq(Order::getId, id)
                .eq(Order::getStatus, 0)
                .set(Order::getStatus, 2));
        if (rows == 0) {
            throw new BusinessException("仅未支付订单可取消");
        }
        order.setStatus(2);
        // 取消后立即放座, 不必等锁租期结束
        seatLockService.unlockSeats(order.getScheduleId(), order.getSeats());
        return fill(order);
    }

    @Override
    public List<String> occupiedSeats(Long scheduleId) {
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getScheduleId, scheduleId)
                .in(Order::getStatus, 0, 1));
        List<String> seats = new ArrayList<>();
        for (Order o : orders) {
            if (o.getSeats() == null || o.getSeats().isEmpty()) {
                continue;
            }
            for (String s : o.getSeats().split(",")) {
                if (!s.isBlank()) {
                    seats.add(s.trim());
                }
            }
        }
        return seats.stream().distinct().collect(Collectors.toList());
    }

    private Order fill(Order order) {
        Schedule schedule = scheduleMapper.selectById(order.getScheduleId());
        if (schedule == null) {
            return order;
        }
        order.setStartTime(schedule.getStartTime());
        Movie movie = movieMapper.selectById(schedule.getMovieId());
        if (movie != null) {
            order.setMovieTitle(movie.getTitle());
            order.setCoverUrl(movie.getCoverUrl());
        }
        Hall hall = hallMapper.selectById(schedule.getHallId());
        if (hall != null) {
            order.setHallName(hall.getName());
        }
        return order;
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FMT)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
