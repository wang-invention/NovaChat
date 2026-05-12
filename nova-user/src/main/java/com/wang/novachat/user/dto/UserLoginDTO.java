package com.wang.novachat.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "登录请求参数")
@Data
public class UserLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "12345678")
    @NotBlank(message = "密码不能为空")
    private String password;
}
