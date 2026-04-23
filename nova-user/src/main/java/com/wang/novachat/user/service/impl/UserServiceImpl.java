package com.wang.novachat.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.common.security.JwtService;
import com.wang.novachat.common.utils.PasswordUtils;
import com.wang.novachat.user.dto.UserLoginDTO;
import com.wang.novachat.user.dto.UserRegisterDTO;
import com.wang.novachat.user.entity.User;
import com.wang.novachat.user.mapper.UserMapper;
import com.wang.novachat.user.service.UserService;
import com.wang.novachat.user.vo.LoginVO;
import com.wang.novachat.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_AVATAR =
            "https://cdn.novachat.example.com/avatar/default.png";

    private final UserMapper userMapper;

    private final JwtService jwtService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserRegisterDTO dto, String registerIp) {
        if (existsByUsername(dto.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }
        if (StrUtil.isNotBlank(dto.getPhone()) && existsByPhone(dto.getPhone())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST, "手机号已注册");
        }
        if (StrUtil.isNotBlank(dto.getEmail()) && existsByEmail(dto.getEmail())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST, "邮箱已注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setNickname(StrUtil.blankToDefault(dto.getNickname(), dto.getUsername()));
        user.setAvatar(DEFAULT_AVATAR);
        user.setPhone(StrUtil.blankToDefault(dto.getPhone(), null));
        user.setEmail(StrUtil.blankToDefault(dto.getEmail(), null));
        user.setGender(0);
        user.setSignature("");
        user.setStatus(1);
        user.setRegisterIp(StrUtil.blankToDefault(registerIp, ""));
        user.setRegisterSource(1);
        user.setLastLoginIp("");

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 兜底并发场景：两个请求同名同时过了 existsByXxx 检查
            log.warn("注册并发冲突：{}", dto.getUsername(), e);
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        log.info("[用户注册] id={}, username={}", user.getId(), user.getUsername());
        return toVO(user);
    }

    @Override
    public LoginVO login(UserLoginDTO dto, String loginIp) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被封禁");
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            // 安全策略：用户名错误也返回相同提示，避免枚举账号
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 更新最后登录信息，失败不影响登录主流程
        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        update.setLastLoginIp(StrUtil.blankToDefault(loginIp, ""));
        update.setVersion(user.getVersion());
        try {
            userMapper.updateById(update);
        } catch (Exception e) {
            log.warn("[用户登录] 最后登录信息更新失败：userId={}", user.getId(), e);
        }

        String token = jwtService.issueToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(token);
        vo.setExpiresAt(System.currentTimeMillis() + jwtService.getExpireSeconds() * 1000);

        log.info("[用户登录] id={}, username={}", user.getId(), user.getUsername());
        return vo;
    }

    // ---------------- private ----------------

    private boolean existsByUsername(String username) {
        return userMapper.exists(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private boolean existsByPhone(String phone) {
        return userMapper.exists(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    private boolean existsByEmail(String email) {
        return userMapper.exists(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
