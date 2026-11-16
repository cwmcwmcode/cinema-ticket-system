package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.R;
import com.library.dto.BookPageQueryDTO;
import com.library.entity.Book;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 图书管理接口
 */
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 新增图书
     */
    @PostMapping("/add")
    public R<Void> add(@RequestBody Book book) {
        bookService.save(book);
        return R.success();
    }

    /**
     * 根据 ID 删除图书
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        bookService.removeById(id);
        return R.success();
    }

    /**
     * 修改图书信息
     */
    @PutMapping("/update")
    public R<Void> update(@RequestBody Book book) {
        bookService.updateById(book);
        return R.success();
    }

    /**
     * 根据 ID 查询图书详情
     */
    @GetMapping("/get/{id}")
    public R<Book> get(@PathVariable Long id) {
        return R.success(bookService.getById(id));
    }

    /**
     * 分页条件查询（书名/作者模糊搜索，分类筛选）
     */
    @PostMapping("/page")
    public R<IPage<Book>> page(@RequestBody BookPageQueryDTO queryDTO) {
        return R.success(bookService.pageQuery(queryDTO));
    }
}
