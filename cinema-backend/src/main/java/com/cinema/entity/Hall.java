package com.cinema.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_hall")
public class Hall {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 影厅名称, 如 1号巨幕厅 */
    private String name;

    private Integer rowCount;

    private Integer colCount;

    private Integer seatTotal;
}
