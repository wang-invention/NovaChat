package com.wang.novachat.user.service;

import com.wang.novachat.user.dto.UserLoginDTO;
import com.wang.novachat.user.dto.UserRegisterDTO;
import com.wang.novachat.user.vo.LoginVO;
import com.wang.novachat.user.vo.UserVO;

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
     */
    LoginVO login(UserLoginDTO dto, String loginIp);
}
