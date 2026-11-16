package com.cinema.controller;

import com.cinema.common.R;
import com.cinema.dto.LoginDTO;
import com.cinema.dto.RegisterDTO;
import com.cinema.entity.SysUser;
import com.cinema.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<SysUser> register(@RequestBody RegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<SysUser> login(@RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }
}
