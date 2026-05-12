package com.wang.novachat.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "用户信息")
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别: 0未知 1男 2女")
    private Integer gender;

    @Schema(description = "状态: 0禁用 1正常")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
