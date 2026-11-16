package com.cinema.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cinema.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 行锁读取场次, 用于同场次并发选座串行化
     */
    @Select("SELECT * FROM t_schedule WHERE id = #{id} FOR UPDATE")
    Schedule selectByIdForUpdate(@Param("id") Long id);
}
