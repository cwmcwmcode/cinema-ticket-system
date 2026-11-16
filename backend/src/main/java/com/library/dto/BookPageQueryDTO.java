package com.library.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 图书分页查询参数
 */
@Data
public class BookPageQueryDTO implements Serializable {

    /**
     * 当前页码，默认第1页
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认10条
     */
    private Integer pageSize = 10;

    /**
     * 书名（模糊搜索）
     */
    private String title;

    /**
     * 作者（模糊搜索）
     */
    private String author;

    /**
     * 分类（精确筛选）
     */
    private String category;
}
