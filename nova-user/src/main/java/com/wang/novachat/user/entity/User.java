package com.wang.novachat.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户账号实体，对齐 Day4 {@code nova_user.t_user} 表结构。
 */
@Data
@TableName("t_user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 密文，序列化出参时务必屏蔽 */
    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    /** 0未知 1男 2女 */
    private Integer gender;

    private LocalDate birthday;

    private String signature;

    /** 1正常 0封禁 2注销 */
    private Integer status;

    private String registerIp;

    /** 1Web 2APP 3小程序 4第三方 */
    private Integer registerSource;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @Version
    private Integer version;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
