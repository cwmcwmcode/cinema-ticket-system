package com.cinema.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinema.entity.Movie;

import java.util.List;

public interface MovieService {

    Page<Movie> page(int pageNum, int pageSize, String keyword);

    List<Movie> listAll();

    Movie getById(Long id);

    Movie save(Movie movie);

    Movie update(Long id, Movie movie);

    void remove(Long id);
}
