package com.wang.novachat.user.controller;

import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.common.utils.IpUtils;
import com.wang.novachat.user.dto.FileUploadResult;
import com.wang.novachat.user.dto.UserLoginDTO;
import com.wang.novachat.user.dto.UserRegisterDTO;
import com.wang.novachat.user.dto.UserUpdateDTO;
import com.wang.novachat.user.service.FileService;
import com.wang.novachat.user.service.UserService;
import com.wang.novachat.user.vo.DeviceVO;
import com.wang.novachat.user.vo.LoginVO;
import com.wang.novachat.user.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    /** 客户端设备类型请求头（可选） */
    private static final String HEADER_X_DEVICE_TYPE = "X-Device-Type";

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
        String deviceId = request.getHeader(CommonConstant.HEADER_X_DEVICE_ID);
        String deviceType = request.getHeader(HEADER_X_DEVICE_TYPE);
        LoginVO vo = userService.login(dto, ip, deviceId, deviceType);
        return Result.success("登录成功", vo);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = getUserId(request);
        String deviceId = request.getHeader(CommonConstant.HEADER_DEVICE_ID);
        if (userId != null && deviceId != null) {
            userService.logout(userId, deviceId);
        }
        return Result.success("登出成功", null);
    }

    @PostMapping("/logout-all")
    public Result<Void> logoutAll(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId != null) {
            userService.logoutAll(userId);
        }
        return Result.success("全部设备已登出", null);
    }

    /**
     * 查询当前用户所有在线设备。
     */
    @GetMapping("/devices")
    public Result<List<DeviceVO>> listDevices(HttpServletRequest request) {
        Long userId = requireUserId(request);
        String currentDeviceId = request.getHeader(CommonConstant.HEADER_DEVICE_ID);
        return Result.success(userService.listDevices(userId, currentDeviceId));
    }

    /**
     * 踢下线指定设备。不能踢当前设备，如需踢自己请走 /logout。
     */
    @PostMapping("/kick")
    public Result<Void> kickDevice(@RequestParam("deviceId") String targetDeviceId,
                                   HttpServletRequest request) {
        Long userId = requireUserId(request);
        String currentDeviceId = request.getHeader(CommonConstant.HEADER_DEVICE_ID);
        userService.kickDevice(userId, currentDeviceId, targetDeviceId);
        return Result.success("已踢下线", null);
    }

    /**
     * 获取当前登录用户的基本信息。
     * 登录态由网关在 JWT 验签 + Redis 会话校验通过后，注入 X-User-Id 头。
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(userService.getCurrentUser(userId));
    }

    @PostMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserUpdateDTO dto,
                                      HttpServletRequest request) {
        Long userId = requireUserId(request);
        userService.updateProfile(userId, dto);
        return Result.success("更新成功", null);
    }

    @PostMapping("/avatar")
    public Result<FileUploadResult> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request) {
        Long userId = requireUserId(request);
        FileUploadResult result = fileService.uploadAvatar(userId, file);
        return Result.success("上传成功", result);
    }

    @PostMapping("/image")
    public Result<FileUploadResult> uploadImage(@RequestParam("file") MultipartFile file,
                                                HttpServletRequest request) {
        Long userId = requireUserId(request);
        FileUploadResult result = fileService.uploadImage(userId, file);
        return Result.success("上传成功", result);
    }

    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    @GetMapping("/search")
    public Result<List<UserVO>> searchUsers(@RequestParam("keyword") String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }

    private Long getUserId(HttpServletRequest request) {
        String header = request.getHeader(CommonConstant.HEADER_USER_ID);
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(header);
        } catch (NumberFormatException e) {
            log.warn("[Controller] 非法 X-User-Id 头：{}", header);
            return null;
        }
    }

    private Long requireUserId(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            throw new com.wang.novachat.common.exception.BusinessException(
                    ResultCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
