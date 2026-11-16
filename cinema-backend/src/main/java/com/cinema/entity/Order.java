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
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long scheduleId;

    /** 座位, 如 1排2座,1排3座 */
    private String seats;

    private BigDecimal totalPrice;

    /** 0-未支付, 1-已支付, 2-已取消 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 以下为展示字段, 不映射数据库 */
    @TableField(exist = false)
    private String movieTitle;

    @TableField(exist = false)
    private String coverUrl;

    @TableField(exist = false)
    private String hallName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private LocalDateTime startTime;
}
