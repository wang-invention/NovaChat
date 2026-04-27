package com.wang.novachat.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与校验。统一在此处理，避免各服务自己造轮子。
 * <p>算法：HS256（对称）。上线后建议切 RS256/ES256 做密钥分发。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_JTI = "jti";
    public static final String CLAIM_DEVICE_ID = "deviceId";

    private final JwtProperties properties;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        byte[] bytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("nova.jwt.secret 长度必须 >= 32 字节（当前 "
                    + bytes.length + " 字节）");
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
        log.info("[JWT] 初始化完成，issuer={}, expireSeconds={}",
                properties.getIssuer(), properties.getExpireSeconds());
    }

    /**
     * 签发 Token。
     *
     * @param userId   用户 ID，存 sub 与自定义 claim
     * @param username 用户名
     * @param jti      Token 唯一标识（UUID），用于 Redis 反查
     * @param deviceId 设备 ID
     */
    public String issueToken(Long userId, String username, String jti, String deviceId) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + properties.getExpireSeconds() * 1000);

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(userId))
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_JTI, jti)
                .claim(CLAIM_DEVICE_ID, deviceId)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并校验 Token，失败抛出 {@link JwtException} 子类。
     * <p>调用方需区分 {@link ExpiredJwtException} 与其他 {@code JwtException}。
     */
    public Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }

    /**
     * 静默校验，失败返回 null。适合过滤器快速分支。
     */
    public Claims tryParse(String token) {
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[JWT] 解析失败：{}", e.getMessage());
            return null;
        }
    }

    /** 从 {@code Authorization: Bearer xxx} 头里抽 Token；无则返回 null */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String prefix = properties.getTokenPrefix();
        if (authorizationHeader.startsWith(prefix)) {
            return authorizationHeader.substring(prefix.length()).trim();
        }
        return authorizationHeader.trim();
    }

    public Long getUserId(Claims claims) {
        Object v = claims.get(CLAIM_USER_ID);
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s) {
            return Long.parseLong(s);
        }
        return null;
    }

    public String getUsername(Claims claims) {
        Object v = claims.get(CLAIM_USERNAME);
        return v == null ? null : v.toString();
    }

    public String getTokenId(Claims claims) {
        Object v = claims.get(CLAIM_JTI);
        return v == null ? null : v.toString();
    }

    public String getDeviceId(Claims claims) {
        Object v = claims.get(CLAIM_DEVICE_ID);
        return v == null ? null : v.toString();
    }

    public long getExpireSeconds() {
        return properties.getExpireSeconds();
    }
}
