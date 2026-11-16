package com.cinema.service;

import com.cinema.dto.OrderCreateDTO;
import com.cinema.entity.Order;

import java.util.List;

public interface OrderService {

    /** 选座锁座并生成订单 */
    Order create(OrderCreateDTO dto);

    List<Order> listByUser(Long userId);

    Order getById(Long id);

    Order pay(Long id);

    Order cancel(Long id);

    /** 查询某场次已被占用的座位 */
    List<String> occupiedSeats(Long scheduleId);
}
