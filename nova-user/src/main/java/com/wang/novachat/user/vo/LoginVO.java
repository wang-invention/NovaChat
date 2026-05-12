package com.wang.novachat.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "登录响应")
@Data
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "访问Token，用于后续接口认证")
    private String token;

    @Schema(description = "Token过期时间戳(毫秒)")
    private Long expiresAt;
}
