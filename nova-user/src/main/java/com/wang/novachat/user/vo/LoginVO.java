package com.wang.novachat.user.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应：基础用户信息 + Token。
 * <p>Day6 的 {@code token} 先用 UUID 占位；Day7 会替换为 JWT 签发。
 */
@Data
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    /** TODO Day7 改为 JWT Token */
    private String token;

    /** Token 过期时间（毫秒时间戳） */
    private Long expiresAt;
}
