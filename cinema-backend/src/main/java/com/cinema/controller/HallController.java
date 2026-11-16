package com.cinema.controller;

import com.cinema.common.R;
import com.cinema.entity.Hall;
import com.cinema.service.HallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "影厅")
@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @Operation(summary = "影厅列表")
    @GetMapping("/list")
    public R<List<Hall>> list() {
        return R.ok(hallService.listAll());
    }
}
