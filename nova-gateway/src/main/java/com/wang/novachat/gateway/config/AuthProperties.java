package com.wang.novachat.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权配置，前缀 {@code nova.auth}。
 * <p>{@link #whiteList} 使用 Ant 风格路径（如 {@code /api/user/users/login}、{@code /health/**}）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "nova.auth")
public class AuthProperties {

    /** 是否开启 JWT 鉴权；线下联调可设为 false 临时关闭 */
    private boolean enabled = true;

    /** 免鉴权白名单（Ant 路径） */
    private List<String> whiteList = new ArrayList<>();
}
