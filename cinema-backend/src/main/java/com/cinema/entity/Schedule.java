package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_schedule")
public class Schedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long movieId;

    private Long hallId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private BigDecimal price;

    /** 以下为展示字段, 不映射数据库 */
    @TableField(exist = false)
    private String movieTitle;

    @TableField(exist = false)
    private String movieCover;

    @TableField(exist = false)
    private String hallName;

    @TableField(exist = false)
    private Integer rowCount;

    @TableField(exist = false)
    private Integer colCount;
}
