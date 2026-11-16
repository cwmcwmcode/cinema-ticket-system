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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order create(OrderCreateDTO dto) {
        if (dto.getUserId() == null || userMapper.selectById(dto.getUserId()) == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getScheduleId() == null) {
            throw new BusinessException("场次不能为空");
        }
        if (dto.getSeats() == null || dto.getSeats().isEmpty()) {
            throw new BusinessException("请选择座位");
        }

        // 对场次行加锁, 保证同一场次选座串行, 避免超卖
        Schedule schedule = scheduleMapper.selectByIdForUpdate(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("场次不存在");
        }
        Hall hall = hallMapper.selectById(schedule.getHallId());
        if (hall == null) {
            throw new BusinessException("影厅不存在");
        }

        List<String> seats = dto.getSeats().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (seats.isEmpty()) {
            throw new BusinessException("请选择座位");
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
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许支付");
        }
        order.setStatus(1);
        orderMapper.updateById(order);
        return fill(order);
    }

    @Override
    public Order cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("仅未支付订单可取消");
        }
        order.setStatus(2);
        orderMapper.updateById(order);
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
