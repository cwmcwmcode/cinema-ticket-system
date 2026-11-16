package com.cinema.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cinema.common.BusinessException;
import com.cinema.dto.LoginDTO;
import com.cinema.dto.RegisterDTO;
import com.cinema.entity.SysUser;
import com.cinema.mapper.SysUserMapper;
import com.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;

    @Override
    public SysUser register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        Long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setRole(0);
        user.setPhone(dto.getPhone());
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public SysUser login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String md5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!md5.equalsIgnoreCase(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        user.setPassword(null);
        return user;
    }
}
