package com.cinema.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinema.common.R;
import com.cinema.entity.Schedule;
import com.cinema.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "场次排片")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "场次分页查询")
    @GetMapping("/page")
    public R<Page<Schedule>> page(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) Long movieId,
                                  @RequestParam(required = false) Long hallId) {
        return R.ok(scheduleService.page(pageNum, pageSize, movieId, hallId));
    }

    @Operation(summary = "某电影的全部场次(走缓存)")
    @GetMapping("/movie/{movieId}")
    public R<List<Schedule>> listByMovie(@PathVariable Long movieId) {
        return R.ok(scheduleService.listByMovie(movieId));
    }

    @Operation(summary = "场次详情")
    @GetMapping("/{id}")
    public R<Schedule> detail(@PathVariable Long id) {
        return R.ok(scheduleService.getById(id));
    }

    @Operation(summary = "新增场次(散场时间缺省按电影时长计算)")
    @PostMapping
    public R<Schedule> save(@RequestBody Schedule schedule) {
        return R.ok(scheduleService.save(schedule));
    }

    @Operation(summary = "修改场次")
    @PutMapping("/{id}")
    public R<Schedule> update(@PathVariable Long id, @RequestBody Schedule schedule) {
        return R.ok(scheduleService.update(id, schedule));
    }

    @Operation(summary = "删除场次")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        scheduleService.remove(id);
        return R.ok();
    }
}
