package com.wang.novachat.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "更新用户信息请求参数")
@Data
public class UserUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称", example = "张三")
    @Size(max = 64, message = "昵称最长 64 位")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式错误")
    private String email;

    @Schema(description = "性别: 0未知 1男 2女", example = "1")
    private Integer gender;
}
