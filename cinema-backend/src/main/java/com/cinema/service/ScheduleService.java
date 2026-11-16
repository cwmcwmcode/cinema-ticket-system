package com.cinema.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinema.entity.Schedule;

import java.util.List;

public interface ScheduleService {

    Page<Schedule> page(int pageNum, int pageSize, Long movieId, Long hallId);

    List<Schedule> listByMovie(Long movieId);

    Schedule getById(Long id);

    Schedule save(Schedule schedule);

    Schedule update(Long id, Schedule schedule);

    void remove(Long id);
}
