package com.wang.novachat.user.service;

import com.wang.novachat.user.dto.UserLoginDTO;
import com.wang.novachat.user.dto.UserRegisterDTO;
import com.wang.novachat.user.vo.DeviceVO;
import com.wang.novachat.user.vo.LoginVO;
import com.wang.novachat.user.vo.UserVO;

import java.util.List;

/**
 * 用户领域服务。
 */
public interface UserService {

    /**
     * 注册新用户，返回用户基础信息。
     *
     * @param dto 注册参数
     * @param registerIp 注册 IP（Controller 从请求中取）
     */
    UserVO register(UserRegisterDTO dto, String registerIp);

    /**
     * 账号密码登录，返回用户信息 + Token。
     *
     * @param dto 登录参数
     * @param loginIp 本次登录 IP
     * @param deviceId 设备 ID（无则自动生成）
     * @param deviceType 设备类型（web/android/ios/pc，可空）
     */
    LoginVO login(UserLoginDTO dto, String loginIp, String deviceId, String deviceType);

    /**
     * 单端登出，删除当前会话。
     *
     * @param userId 用户 ID
     * @param deviceId 设备 ID
     */
    void logout(Long userId, String deviceId);

    /**
     * 全部设备踢下线。
     *
     * @param userId 用户 ID
     */
    void logoutAll(Long userId);

    /**
     * 列出该用户当前在线的所有设备。
     *
     * @param userId 用户 ID
     * @param currentDeviceId 当前请求所在设备 ID，用于标记 current 字段
     */
    List<DeviceVO> listDevices(Long userId, String currentDeviceId);

    /**
     * 踢掉指定设备（不允许踢自己，避免用户误把当前登录的 Token 作废）。
     *
     * @param userId 用户 ID
     * @param currentDeviceId 当前请求所在设备 ID
     * @param targetDeviceId 要踢掉的设备 ID
     */
    void kickDevice(Long userId, String currentDeviceId, String targetDeviceId);

    /**
     * 获取当前登录用户的信息。
     *
     * @param userId 用户 ID（从网关透传头获取）
     */
    UserVO getCurrentUser(Long userId);
}
