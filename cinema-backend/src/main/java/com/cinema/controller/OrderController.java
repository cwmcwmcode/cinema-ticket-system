package com.cinema.controller;

import com.cinema.common.R;
import com.cinema.dto.OrderCreateDTO;
import com.cinema.entity.Order;
import com.cinema.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "选座订单")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "选座锁座并生成订单")
    @PostMapping
    public R<Order> create(@RequestBody OrderCreateDTO dto) {
        return R.ok(orderService.create(dto));
    }

    @Operation(summary = "查询场次已占用座位")
    @GetMapping("/occupied")
    public R<List<String>> occupied(@RequestParam Long scheduleId) {
        return R.ok(orderService.occupiedSeats(scheduleId));
    }

    @Operation(summary = "个人订单列表")
    @GetMapping("/user/{userId}")
    public R<List<Order>> listByUser(@PathVariable Long userId) {
        return R.ok(orderService.listByUser(userId));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public R<Order> detail(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @Operation(summary = "支付订单")
    @PutMapping("/{id}/pay")
    public R<Order> pay(@PathVariable Long id) {
        return R.ok(orderService.pay(id));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public R<Order> cancel(@PathVariable Long id) {
        return R.ok(orderService.cancel(id));
    }
}
