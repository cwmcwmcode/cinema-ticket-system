package com.cinema.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinema.common.BusinessException;
import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Schedule;
import com.cinema.mapper.HallMapper;
import com.cinema.mapper.MovieMapper;
import com.cinema.mapper.ScheduleMapper;
import com.cinema.service.CacheService;
import com.cinema.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private static final String CACHE_PREFIX = "schedule:";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ScheduleMapper scheduleMapper;
    private final MovieMapper movieMapper;
    private final HallMapper hallMapper;
    private final CacheService cacheService;

    @Override
    public Page<Schedule> page(int pageNum, int pageSize, Long movieId, Long hallId) {
        LambdaQueryWrapper<Schedule> wrapper = Wrappers.<Schedule>lambdaQuery()
                .eq(movieId != null, Schedule::getMovieId, movieId)
                .eq(hallId != null, Schedule::getHallId, hallId)
                .orderByDesc(Schedule::getStartTime);
        Page<Schedule> page = scheduleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        enrich(page.getRecords());
        return page;
    }

    @Override
    public List<Schedule> listByMovie(Long movieId) {
        String key = CACHE_PREFIX + "movie:" + movieId;
        List<Schedule> cached = cacheService.getList(key, Schedule.class);
        if (cached != null) {
            return cached;
        }
        List<Schedule> list = scheduleMapper.selectList(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getMovieId, movieId)
                .orderByAsc(Schedule::getStartTime));
        enrich(list);
        cacheService.set(key, list, 5);
        return list;
    }

    @Override
    public Schedule getById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("场次不存在");
        }
        enrich(List.of(schedule));
        return schedule;
    }

    @Override
    public Schedule save(Schedule schedule) {
        Movie movie = movieMapper.selectById(schedule.getMovieId());
        if (movie == null) {
            throw new BusinessException("电影不存在");
        }
        Hall hall = hallMapper.selectById(schedule.getHallId());
        if (hall == null) {
            throw new BusinessException("影厅不存在");
        }
        if (schedule.getStartTime() == null) {
            throw new BusinessException("开场时间不能为空");
        }
        if (schedule.getEndTime() == null) {
            schedule.setEndTime(schedule.getStartTime().plusMinutes(movie.getDuration()));
        }
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new BusinessException("散场时间必须晚于开场时间");
        }
        if (schedule.getPrice() == null) {
            schedule.setPrice(movie.getPrice() == null ? BigDecimal.ZERO : movie.getPrice());
        }
        // 同影厅排片时间不可重叠
        checkTimeCollision(schedule.getHallId(), schedule.getStartTime(), schedule.getEndTime(), null);
        scheduleMapper.insert(schedule);
        cacheService.deleteByPrefix(CACHE_PREFIX);
        return schedule;
    }

    @Override
    public Schedule update(Long id, Schedule schedule) {
        Schedule exist = scheduleMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("场次不存在");
        }
        schedule.setId(id);
        if (schedule.getMovieId() == null) {
            schedule.setMovieId(exist.getMovieId());
        }
        if (schedule.getHallId() == null) {
            schedule.setHallId(exist.getHallId());
        }
        if (schedule.getStartTime() == null) {
            schedule.setStartTime(exist.getStartTime());
        }
        if (schedule.getEndTime() == null) {
            Movie movie = movieMapper.selectById(schedule.getMovieId());
            int duration = movie == null || movie.getDuration() == null ? 0 : movie.getDuration();
            schedule.setEndTime(schedule.getStartTime().plusMinutes(duration));
        }
        // 调整后的排片同样不允许与同影厅其它场次重叠
        checkTimeCollision(schedule.getHallId(), schedule.getStartTime(), schedule.getEndTime(), id);
        scheduleMapper.updateById(schedule);
        cacheService.deleteByPrefix(CACHE_PREFIX);
        return getById(id);
    }

    @Override
    public void remove(Long id) {
        if (scheduleMapper.selectById(id) == null) {
            throw new BusinessException("场次不存在");
        }
        scheduleMapper.deleteById(id);
        cacheService.deleteByPrefix(CACHE_PREFIX);
    }

    /**
     * 排片时间碰撞检测: 同影厅下 (startTime < 已有散场) 且 (endTime > 已有开场) 即视为重叠。
     * 仅做普通查询, 不依赖 RR 快照读即可拦截绝大多数冲突。
     */
    private void checkTimeCollision(Long hallId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        List<Schedule> conflicts = scheduleMapper.selectList(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getHallId, hallId)
                .ne(excludeId != null, Schedule::getId, excludeId)
                .lt(Schedule::getStartTime, endTime)
                .gt(Schedule::getEndTime, startTime));
        if (!conflicts.isEmpty()) {
            Schedule exist = conflicts.get(0);
            throw new BusinessException("该影厅排片时间冲突: 已有 "
                    + exist.getStartTime().format(TIME_FMT) + " ~ " + exist.getEndTime().format(TIME_FMT) + " 的场次");
        }
    }

    /**
     * 补齐电影名/影厅名/座位行列数, 供前端渲染选座图
     */
    private void enrich(List<Schedule> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> movieIds = list.stream().map(Schedule::getMovieId).distinct().toList();
        List<Long> hallIds = list.stream().map(Schedule::getHallId).distinct().toList();
        Map<Long, Movie> movies = movieMapper.selectBatchIds(movieIds).stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity()));
        Map<Long, Hall> halls = hallMapper.selectBatchIds(hallIds).stream()
                .collect(Collectors.toMap(Hall::getId, Function.identity()));
        for (Schedule s : list) {
            Movie movie = movies.get(s.getMovieId());
            Hall hall = halls.get(s.getHallId());
            if (movie != null) {
                s.setMovieTitle(movie.getTitle());
                s.setMovieCover(movie.getCoverUrl());
            }
            if (hall != null) {
                s.setHallName(hall.getName());
                s.setRowCount(hall.getRowCount());
                s.setColCount(hall.getColCount());
            }
        }
    }
}
