package com.wang.novachat.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 登录会话 Redis 读写。
 * <p>
 * 键设计：
 * <ul>
 *   <li>{@code user:login:token:{tokenId}} - String，tokenId -> 会话明细，网关快速校验</li>
 *   <li>{@code user:login:session:{userId}:{deviceId}} - String，业务侧按用户+设备定位</li>
 *   <li>{@code user:login:devices:{userId}} - Set，该用户所有活跃 deviceId，用于多端管理</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSessionService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 保存一个登录会话，并把设备加入该用户的设备集合。
     */
    public void save(LoginSession session, long ttlSeconds) {
        String tokenKey = RedisKeys.token(session.getTokenId());
        String sessionKey = RedisKeys.session(session.getUserId(), session.getDeviceId());
        String devicesKey = RedisKeys.devices(session.getUserId());

        String sessionData = buildSessionData(session);

        stringRedisTemplate.opsForValue().set(tokenKey, sessionData, ttlSeconds, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(sessionKey, sessionData, ttlSeconds, TimeUnit.SECONDS);
        stringRedisTemplate.opsForSet().add(devicesKey, session.getDeviceId());
        // 设备集合给一个略长于单 session 的过期时间，避免僵尸集合长期驻留
        stringRedisTemplate.expire(devicesKey, ttlSeconds + 3600, TimeUnit.SECONDS);

        log.debug("[Session] 保存会话 userId={}, deviceId={}, tokenId={}, ttl={}s",
                session.getUserId(), session.getDeviceId(), session.getTokenId(), ttlSeconds);
    }

    public LoginSession getByTokenId(String tokenId) {
        String key = RedisKeys.token(tokenId);
        String data = stringRedisTemplate.opsForValue().get(key);
        if (data == null) {
            return null;
        }
        return parseSessionData(data);
    }

    public LoginSession getByUserAndDevice(Long userId, String deviceId) {
        String key = RedisKeys.session(userId, deviceId);
        String data = stringRedisTemplate.opsForValue().get(key);
        if (data == null) {
            return null;
        }
        return parseSessionData(data);
    }

    /**
     * 移除指定设备的会话：删 token、删 session、并从设备集合里 SREM。
     */
    public void remove(Long userId, String deviceId) {
        LoginSession session = getByUserAndDevice(userId, deviceId);
        if (session != null && session.getTokenId() != null) {
            stringRedisTemplate.delete(RedisKeys.token(session.getTokenId()));
        }
        stringRedisTemplate.delete(RedisKeys.session(userId, deviceId));
        stringRedisTemplate.opsForSet().remove(RedisKeys.devices(userId), deviceId);

        log.info("[Session] 移除会话 userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 移除该用户全部设备会话。遍历设备集合精确删除，不使用 keys() 扫描。
     */
    public void removeAll(Long userId) {
        String devicesKey = RedisKeys.devices(userId);
        Set<String> deviceIds = stringRedisTemplate.opsForSet().members(devicesKey);
        if (deviceIds != null) {
            for (String deviceId : deviceIds) {
                LoginSession s = getByUserAndDevice(userId, deviceId);
                if (s != null && s.getTokenId() != null) {
                    stringRedisTemplate.delete(RedisKeys.token(s.getTokenId()));
                }
                stringRedisTemplate.delete(RedisKeys.session(userId, deviceId));
            }
        }
        stringRedisTemplate.delete(devicesKey);

        log.info("[Session] 移除全部会话 userId={}, count={}", userId,
                deviceIds == null ? 0 : deviceIds.size());
    }

    /**
     * 保留指定设备，踢掉该用户其他所有设备。用于"仅保留当前设备登录"场景。
     */
    public void removeOthers(Long userId, String keepDeviceId) {
        Set<String> deviceIds = stringRedisTemplate.opsForSet()
                .members(RedisKeys.devices(userId));
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        for (String deviceId : deviceIds) {
            if (deviceId.equals(keepDeviceId)) {
                continue;
            }
            remove(userId, deviceId);
        }
    }

    /**
     * 列出该用户当前所有活跃会话。顺便做懒清理：发现设备 Set 里有但 session 已过期的，SREM 剔除。
     */
    public List<LoginSession> listSessions(Long userId) {
        String devicesKey = RedisKeys.devices(userId);
        Set<String> deviceIds = stringRedisTemplate.opsForSet().members(devicesKey);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<LoginSession> sessions = new ArrayList<>(deviceIds.size());
        List<String> expired = new ArrayList<>();
        for (String deviceId : deviceIds) {
            LoginSession s = getByUserAndDevice(userId, deviceId);
            if (s == null) {
                expired.add(deviceId);
            } else {
                sessions.add(s);
            }
        }
        if (!expired.isEmpty()) {
            stringRedisTemplate.opsForSet().remove(devicesKey, expired.toArray());
            log.debug("[Session] 懒清理过期设备 userId={}, expired={}", userId, expired);
        }
        sessions.sort(Comparator.comparing(
                LoginSession::getLoginAt,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return sessions;
    }

    /**
     * 当前在线设备数（带懒清理）。
     */
    public int countActiveDevices(Long userId) {
        return listSessions(userId).size();
    }

    public void refreshTtl(String tokenId, long ttlSeconds) {
        String key = RedisKeys.token(tokenId);
        Boolean success = stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("[Session] 续期 tokenId={}, ttl={}s", tokenId, ttlSeconds);
        }
    }

    public Long getUserIdByTokenId(String tokenId) {
        LoginSession session = getByTokenId(tokenId);
        return session != null ? session.getUserId() : null;
    }

    public String getDeviceIdByTokenId(String tokenId) {
        LoginSession session = getByTokenId(tokenId);
        return session != null ? session.getDeviceId() : null;
    }

    private static final String SEPARATOR = "|";

    private String buildSessionData(LoginSession session) {
        return String.join(SEPARATOR,
                String.valueOf(session.getUserId()),
                session.getUsername() != null ? session.getUsername() : "",
                session.getDeviceId() != null ? session.getDeviceId() : "",
                session.getDeviceType() != null ? session.getDeviceType() : "",
                session.getIp() != null ? session.getIp() : "",
                session.getLoginAt() != null ? session.getLoginAt().toString() : "",
                session.getExpireAt() != null ? session.getExpireAt().toString() : "",
                session.getTokenId() != null ? session.getTokenId() : "");
    }

    private LoginSession parseSessionData(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        String[] parts = data.split(java.util.regex.Pattern.quote(SEPARATOR), 8);
        if (parts.length < 8) {
            log.warn("[Session] 会话数据格式错误：{}", data);
            return null;
        }
        try {
            return LoginSession.builder()
                    .userId(Long.parseLong(parts[0]))
                    .username(parts[1].isEmpty() ? null : parts[1])
                    .deviceId(parts[2].isEmpty() ? null : parts[2])
                    .deviceType(parts[3].isEmpty() ? null : parts[3])
                    .ip(parts[4].isEmpty() ? null : parts[4])
                    .loginAt(parts[5].isEmpty() ? null : java.time.LocalDateTime.parse(parts[5]))
                    .expireAt(parts[6].isEmpty() ? null : java.time.LocalDateTime.parse(parts[6]))
                    .tokenId(parts[7].isEmpty() ? null : parts[7])
                    .build();
        } catch (Exception e) {
            log.warn("[Session] 解析会话数据失败：{}", data, e);
            return null;
        }
    }
}
