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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户服务", description = "用户注册、登录、资料管理接口")
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    @Operation(summary = "用户注册", description = "新用户注册账号，注册成功后自动登录并返回Token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败")
    })
    @PostMapping("/register")
    public Result<LoginVO> register(
            @Valid @RequestBody @Parameter(description = "注册信息", required = true) UserRegisterDTO dto,
            HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        String deviceId = request.getHeader(CommonConstant.HEADER_X_DEVICE_ID);
        LoginVO vo = userService.register(dto, ip, deviceId);
        return Result.success("注册成功", vo);
    }

    @Operation(summary = "用户登录", description = "用户名密码登录，返回Token用于后续接口认证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public Result<LoginVO> login(
            @Valid @RequestBody @Parameter(description = "登录信息", required = true) UserLoginDTO dto,
            HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        String deviceId = request.getHeader(CommonConstant.HEADER_X_DEVICE_ID);
        String deviceType = request.getHeader("X-Device-Type");
        LoginVO vo = userService.login(dto, ip, deviceId, deviceType);
        return Result.success("登录成功", vo);
    }

    @Operation(summary = "用户登出", description = "当前设备下线，Token失效")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = getUserId(request);
        String deviceId = request.getHeader(CommonConstant.HEADER_DEVICE_ID);
        if (userId != null && deviceId != null) {
            userService.logout(userId, deviceId);
        }
        return Result.success("登出成功", null);
    }

    @Operation(summary = "全部设备登出", description = "该用户所有设备全部下线")
    @PostMapping("/logout-all")
    public Result<Void> logoutAll(HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId != null) {
            userService.logoutAll(userId);
        }
        return Result.success("全部设备已登出", null);
    }

    @Operation(summary = "查询在线设备", description = "获取当前用户所有已登录的设备列表")
    @GetMapping("/devices")
    public Result<List<DeviceVO>> listDevices(HttpServletRequest request) {
        Long userId = requireUserId(request);
        String currentDeviceId = request.getHeader(CommonConstant.HEADER_DEVICE_ID);
        return Result.success(userService.listDevices(userId, currentDeviceId));
    }

    @Operation(summary = "踢下线设备", description = "将指定设备踢下线，不能踢当前设备")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "已踢下线"),
            @ApiResponse(responseCode = "400", description = "不能踢当前设备")
    })
    @PostMapping("/kick")
    public Result<Void> kickDevice(
            @Parameter(description = "目标设备ID", required = true) @RequestParam("deviceId") String targetDeviceId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        String currentDeviceId = request.getHeader(CommonConstant.HEADER_DEVICE_ID);
        userService.kickDevice(userId, currentDeviceId, targetDeviceId);
        return Result.success("已踢下线", null);
    }

    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的基本信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(userService.getCurrentUser(userId));
    }

    @Operation(summary = "更新个人资料", description = "更新昵称、手机号、邮箱、性别等个人信息")
    @PostMapping("/profile")
    public Result<Void> updateProfile(
            @RequestBody @Parameter(description = "更新信息", required = true) UserUpdateDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        userService.updateProfile(userId, dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "上传头像", description = "上传用户头像图片，返回图片URL")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadResult> uploadAvatar(
            @Parameter(description = "头像图片文件", required = true) @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        FileUploadResult result = fileService.uploadAvatar(userId, file);
        return Result.success("上传成功", result);
    }

    @Operation(summary = "上传图片", description = "上传聊天图片，返回图片URL")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadResult> uploadImage(
            @Parameter(description = "图片文件", required = true) @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        FileUploadResult result = fileService.uploadImage(userId, file);
        return Result.success("上传成功", result);
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户基本信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，返回匹配的用户列表")
    @GetMapping("/search")
    public Result<List<UserVO>> searchUsers(
            @Parameter(description = "搜索关键词(用户名/昵称)", required = true) @RequestParam("keyword") String keyword) {
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
