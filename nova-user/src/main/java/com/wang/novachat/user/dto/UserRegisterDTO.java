package com.wang.novachat.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "注册请求参数")
@Data
public class UserRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名(字母开头,仅字母/数字/下划线)", example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度需在 4~32 位")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{3,31}$",
            message = "用户名需以字母开头，仅允许字母/数字/下划线")
    private String username;

    @Schema(description = "密码(8-32位)", example = "12345678")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在 8~32 位")
    private String password;

    @Schema(description = "昵称", example = "张三")
    @Size(max = 64, message = "昵称最长 64 位")
    private String nickname;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式错误")
    private String email;
}
