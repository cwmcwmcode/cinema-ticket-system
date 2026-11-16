package com.library.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.library.dto.BookPageQueryDTO;
import com.library.entity.Book;

public interface BookService extends IService<Book> {

    /**
     * 分页条件查询
     */
    IPage<Book> pageQuery(BookPageQueryDTO queryDTO);
}
