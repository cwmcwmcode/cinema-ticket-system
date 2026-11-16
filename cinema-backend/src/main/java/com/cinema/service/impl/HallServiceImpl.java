package com.cinema.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cinema.entity.Hall;
import com.cinema.mapper.HallMapper;
import com.cinema.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallMapper hallMapper;

    @Override
    public List<Hall> listAll() {
        return hallMapper.selectList(Wrappers.<Hall>lambdaQuery().orderByAsc(Hall::getId));
    }
}
