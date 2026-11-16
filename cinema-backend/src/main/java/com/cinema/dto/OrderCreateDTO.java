package com.cinema.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "选座下单参数")
public class OrderCreateDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "场次ID")
    private Long scheduleId;

    @Schema(description = "座位列表, 如 [\"1排2座\",\"1排3座\"]")
    private List<String> seats;
}
