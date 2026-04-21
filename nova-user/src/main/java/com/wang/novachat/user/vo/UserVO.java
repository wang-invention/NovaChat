package com.wang.novachat.user.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息出参：永远不要把 {@code password} 暴露出去。
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    private Integer gender;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;
}
