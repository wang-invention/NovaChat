package com.wang.novachat.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 全局配置，前缀 {@code nova.jwt}。
 * <p>推荐把 {@code secret} 放 Nacos / 环境变量，避免提交进仓库。
 */
@Data
@Component
@ConfigurationProperties(prefix = "nova.jwt")
public class JwtProperties {

    /**
     * HS256 对称密钥，至少 256 bit（32 字节）。
     * 线上务必通过 env 覆盖：NOVA_JWT_SECRET=xxx
     */
    private String secret = "nova-chat-jwt-secret-change-me-in-production-2026";

    /** Token 有效期（秒），默认 7 天 */
    private long expireSeconds = 7L * 24 * 60 * 60;

    /** 签发者 */
    private String issuer = "nova-chat";

    /** 请求头 key */
    private String header = "Authorization";

    /** Token 前缀，识别 / 截取用 */
    private String tokenPrefix = "Bearer ";
}
