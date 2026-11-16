package com.cinema.service;

import com.cinema.dto.LoginDTO;
import com.cinema.dto.RegisterDTO;
import com.cinema.entity.SysUser;

public interface UserService {

    SysUser register(RegisterDTO dto);

    SysUser login(LoginDTO dto);
}
