package com.wang.novachat.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.common.security.JwtService;
import com.wang.novachat.common.security.LoginSession;
import com.wang.novachat.common.security.LoginSessionService;
import com.wang.novachat.common.utils.PasswordUtils;
import com.wang.novachat.user.dto.UserLoginDTO;
import com.wang.novachat.user.dto.UserRegisterDTO;
import com.wang.novachat.user.dto.UserUpdateDTO;
import com.wang.novachat.user.entity.User;
import com.wang.novachat.user.mapper.UserMapper;
import com.wang.novachat.user.service.UserService;
import com.wang.novachat.user.vo.DeviceVO;
import com.wang.novachat.user.vo.LoginVO;
import com.wang.novachat.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_AVATAR =
            "https://cdn.novachat.example.com/avatar/default.png";

    private final UserMapper userMapper;

    private final JwtService jwtService;

    private final LoginSessionService loginSessionService;

    /** 登录模式：single=单端登录（新登录踢掉旧的全部），multi=多端共存 */
    @Value("${nova.login.mode:single}")
    private String loginMode;

    /** multi 模式下同一账号允许的最大在线设备数，超限按登录时间淘汰最老 */
    @Value("${nova.login.max-devices:3}")
    private int maxDevices;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(UserRegisterDTO dto, String registerIp) {
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
            log.warn("注册并发冲突：{}", dto.getUsername(), e);
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        String deviceId = IdUtil.fastSimpleUUID();
        String tokenId = IdUtil.fastSimpleUUID();
        String token = jwtService.issueToken(user.getId(), user.getUsername(), tokenId, deviceId);
        long ttlSeconds = jwtService.getExpireSeconds();
        LocalDateTime now = LocalDateTime.now();

        enforceMaxDevices(user.getId(), deviceId);

        LoginSession session = LoginSession.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .deviceId(deviceId)
                .ip(registerIp)
                .loginAt(now)
                .expireAt(now.plusSeconds(ttlSeconds))
                .tokenId(tokenId)
                .build();
        loginSessionService.save(session, ttlSeconds);

        log.info("[用户注册] id={}, username={}", user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(token);
        vo.setExpiresAt(System.currentTimeMillis() + ttlSeconds * 1000);
        return vo;
    }

    @Override
    public LoginVO login(UserLoginDTO dto, String loginIp, String deviceId, String deviceType) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被封禁");
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        if (StrUtil.isBlank(deviceId)) {
            deviceId = IdUtil.fastSimpleUUID();
        }

        String tokenId = IdUtil.fastSimpleUUID();
        String token = jwtService.issueToken(user.getId(), user.getUsername(), tokenId, deviceId);
        long ttlSeconds = jwtService.getExpireSeconds();
        LocalDateTime now = LocalDateTime.now();

        if ("single".equals(loginMode)) {
            // 单端登录：先干掉该用户所有旧会话（包括当前复用的 deviceId，保证 tokenId 唯一）
            loginSessionService.removeAll(user.getId());
        } else {
            // 多端登录：超过上限按登录时间淘汰最老的设备（若当前 deviceId 已存在则只做覆盖）
            enforceMaxDevices(user.getId(), deviceId);
        }

        LoginSession session = LoginSession.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .deviceId(deviceId)
                .deviceType(StrUtil.blankToDefault(deviceType, null))
                .ip(loginIp)
                .loginAt(now)
                .expireAt(now.plusSeconds(ttlSeconds))
                .tokenId(tokenId)
                .build();
        loginSessionService.save(session, ttlSeconds);

        User update = new User();
        update.setId(user.getId());
        update.setLastLoginTime(now);
        update.setLastLoginIp(StrUtil.blankToDefault(loginIp, ""));
        update.setVersion(user.getVersion());
        try {
            userMapper.updateById(update);
        } catch (Exception e) {
            log.warn("[用户登录] 最后登录信息更新失败：userId={}", user.getId(), e);
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setToken(token);
        vo.setExpiresAt(System.currentTimeMillis() + ttlSeconds * 1000);

        log.info("[用户登录] id={}, username={}, deviceId={}, mode={}",
                user.getId(), user.getUsername(), deviceId, loginMode);
        return vo;
    }

    @Override
    public void logout(Long userId, String deviceId) {
        loginSessionService.remove(userId, deviceId);
        log.info("[用户登出] userId={}, deviceId={}", userId, deviceId);
    }

    @Override
    public void logoutAll(Long userId) {
        loginSessionService.removeAll(userId);
        log.info("[用户全部登出] userId={}", userId);
    }

    @Override
    public List<DeviceVO> listDevices(Long userId, String currentDeviceId) {
        List<LoginSession> sessions = loginSessionService.listSessions(userId);
        List<DeviceVO> list = new ArrayList<>(sessions.size());
        for (LoginSession s : sessions) {
            DeviceVO vo = new DeviceVO();
            vo.setDeviceId(s.getDeviceId());
            vo.setDeviceType(s.getDeviceType());
            vo.setIp(s.getIp());
            vo.setLoginAt(s.getLoginAt());
            vo.setExpireAt(s.getExpireAt());
            vo.setCurrent(s.getDeviceId() != null && s.getDeviceId().equals(currentDeviceId));
            list.add(vo);
        }
        return list;
    }

    @Override
    public void kickDevice(Long userId, String currentDeviceId, String targetDeviceId) {
        if (StrUtil.isBlank(targetDeviceId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "deviceId 不能为空");
        }
        if (targetDeviceId.equals(currentDeviceId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能踢下线当前设备，请使用登出接口");
        }
        LoginSession session = loginSessionService.getByUserAndDevice(userId, targetDeviceId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标设备不在线");
        }
        loginSessionService.remove(userId, targetDeviceId);
        log.info("[踢设备] operator={}, target={}, userId={}",
                currentDeviceId, targetDeviceId, userId);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return toVO(user);
    }

    @Override
    public void updateProfile(Long userId, UserUpdateDTO dto) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        User update = new User();
        update.setId(userId);
        update.setVersion(user.getVersion());

        if (dto.getNickname() != null) {
            update.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            update.setAvatar(dto.getAvatar());
        }
        if (dto.getPhone() != null) {
            update.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            update.setEmail(dto.getEmail());
        }
        if (dto.getGender() != null) {
            update.setGender(dto.getGender());
        }

        userMapper.updateById(update);
        log.info("[更新用户资料] userId={}", userId);
    }

    @Override
    public List<UserVO> searchUsers(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .or()
                .like(User::getPhone, keyword)
                .last("LIMIT 20");
        List<User> users = userMapper.selectList(wrapper);
        return users.stream().map(this::toVO).collect(java.util.stream.Collectors.toList());
    }

    // ---------------- private ----------------

    /**
     * 多端登录模式下，保证加上新设备之后不超过 maxDevices。
     * 若 newDeviceId 已在集合里，视为覆盖，不挤占名额；否则按 loginAt 淘汰最老。
     */
    private void enforceMaxDevices(Long userId, String newDeviceId) {
        if (maxDevices <= 0) {
            return;
        }
        List<LoginSession> sessions = loginSessionService.listSessions(userId);
        boolean replacing = sessions.stream()
                .anyMatch(s -> newDeviceId.equals(s.getDeviceId()));
        int afterCount = replacing ? sessions.size() : sessions.size() + 1;
        if (afterCount <= maxDevices) {
            return;
        }

        List<LoginSession> candidates = new ArrayList<>(sessions);
        if (replacing) {
            candidates.removeIf(s -> newDeviceId.equals(s.getDeviceId()));
        }
        candidates.sort(Comparator.comparing(
                LoginSession::getLoginAt,
                Comparator.nullsFirst(Comparator.naturalOrder())));

        int toEvict = afterCount - maxDevices;
        for (int i = 0; i < toEvict && i < candidates.size(); i++) {
            LoginSession old = candidates.get(i);
            loginSessionService.remove(userId, old.getDeviceId());
            log.info("[踢设备-超限淘汰] userId={}, evicted={}, loginAt={}",
                    userId, old.getDeviceId(), old.getLoginAt());
        }
    }

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
