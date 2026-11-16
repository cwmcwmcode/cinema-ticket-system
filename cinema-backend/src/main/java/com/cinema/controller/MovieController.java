package com.cinema.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cinema.common.R;
import com.cinema.entity.Movie;
import com.cinema.service.MovieService;
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

@Tag(name = "电影管理")
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @Operation(summary = "电影分页查询")
    @GetMapping("/page")
    public R<Page<Movie>> page(@RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String keyword) {
        return R.ok(movieService.page(pageNum, pageSize, keyword));
    }

    @Operation(summary = "电影列表(首页/下拉框, 走缓存)")
    @GetMapping("/list")
    public R<List<Movie>> list() {
        return R.ok(movieService.listAll());
    }

    @Operation(summary = "电影详情")
    @GetMapping("/{id}")
    public R<Movie> detail(@PathVariable Long id) {
        return R.ok(movieService.getById(id));
    }

    @Operation(summary = "新增电影")
    @PostMapping
    public R<Movie> save(@RequestBody Movie movie) {
        return R.ok(movieService.save(movie));
    }

    @Operation(summary = "修改电影")
    @PutMapping("/{id}")
    public R<Movie> update(@PathVariable Long id, @RequestBody Movie movie) {
        return R.ok(movieService.update(id, movie));
    }

    @Operation(summary = "删除电影")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        movieService.remove(id);
        return R.ok();
    }
}
