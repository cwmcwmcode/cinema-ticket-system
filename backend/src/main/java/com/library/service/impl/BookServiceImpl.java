package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.dto.BookPageQueryDTO;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import com.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public IPage<Book> pageQuery(BookPageQueryDTO queryDTO) {
        Page<Book> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        // 书名模糊搜索
        wrapper.like(StringUtils.hasText(queryDTO.getTitle()), Book::getTitle, queryDTO.getTitle());
        // 作者模糊搜索
        wrapper.like(StringUtils.hasText(queryDTO.getAuthor()), Book::getAuthor, queryDTO.getAuthor());
        // 分类精确筛选
        wrapper.eq(StringUtils.hasText(queryDTO.getCategory()), Book::getCategory, queryDTO.getCategory());
        // 按创建时间倒序
        wrapper.orderByDesc(Book::getCreateTime);

        return this.page(page, wrapper);
    }
}
