package com.cinema.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinema.common.BusinessException;
import com.cinema.entity.Movie;
import com.cinema.mapper.MovieMapper;
import com.cinema.service.CacheService;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private static final String CACHE_ALL = "movie:list";
    private static final String CACHE_PREFIX = "movie:";

    private final MovieMapper movieMapper;
    private final CacheService cacheService;

    @Override
    public Page<Movie> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<Movie> wrapper = Wrappers.<Movie>lambdaQuery()
                .like(StringUtils.hasText(keyword), Movie::getTitle, keyword)
                .orderByDesc(Movie::getId);
        return movieMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Movie> listAll() {
        List<Movie> cached = cacheService.getList(CACHE_ALL, Movie.class);
        if (cached != null) {
            return cached;
        }
        List<Movie> list = movieMapper.selectList(Wrappers.<Movie>lambdaQuery().orderByDesc(Movie::getId));
        cacheService.set(CACHE_ALL, list, 10);
        return list;
    }

    @Override
    public Movie getById(Long id) {
        Movie cached = cacheService.get(CACHE_PREFIX + "detail:" + id, Movie.class);
        if (cached != null) {
            return cached;
        }
        Movie movie = movieMapper.selectById(id);
        if (movie == null) {
            throw new BusinessException("电影不存在");
        }
        cacheService.set(CACHE_PREFIX + "detail:" + id, movie, 10);
        return movie;
    }

    @Override
    public Movie save(Movie movie) {
        validate(movie);
        movieMapper.insert(movie);
        cacheService.deleteByPrefix(CACHE_PREFIX);
        return movie;
    }

    @Override
    public Movie update(Long id, Movie movie) {
        getById(id);
        movie.setId(id);
        validate(movie);
        movieMapper.updateById(movie);
        cacheService.deleteByPrefix(CACHE_PREFIX);
        return movieMapper.selectById(id);
    }

    @Override
    public void remove(Long id) {
        getById(id);
        movieMapper.deleteById(id);
        cacheService.deleteByPrefix(CACHE_PREFIX);
    }

    private void validate(Movie movie) {
        if (!StringUtils.hasText(movie.getTitle())) {
            throw new BusinessException("电影名称不能为空");
        }
        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new BusinessException("电影时长必须大于 0");
        }
    }
}
