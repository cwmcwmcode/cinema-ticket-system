package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 图书实体
 */
@Data
@TableName("tb_book")
public class Book implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 书名
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * ISBN号
     */
    private String isbn;

    /**
     * 分类
     */
    private String category;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 状态：0-下架, 1-在架
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
