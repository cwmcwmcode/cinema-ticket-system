package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("t_movie")
public class Movie {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String director;

    private String actors;

    /** 时长(分钟) */
    private Integer duration;

    private String coverUrl;

    private String description;

    private BigDecimal price;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
}
