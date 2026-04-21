package com.wang.novachat.user.controller;

import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.utils.IpUtils;
import com.wang.novachat.user.dto.UserLoginDTO;
import com.wang.novachat.user.dto.UserRegisterDTO;
import com.wang.novachat.user.service.UserService;
import com.wang.novachat.user.vo.LoginVO;
import com.wang.novachat.user.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserRegisterDTO dto,
                                   HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        UserVO vo = userService.register(dto, ip);
        return Result.success("注册成功", vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto,
                                 HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        LoginVO vo = userService.login(dto, ip);
        return Result.success("登录成功", vo);
    }
}
