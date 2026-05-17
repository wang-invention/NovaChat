package com.wang.novachat.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "群成员信息")
@Data
public class GroupMemberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "角色: 0成员 1管理员 2群主")
    private Integer role;

    @Schema(description = "群内昵称")
    private String nicknameInGroup;

    @Schema(description = "禁言截止时间")
    private LocalDateTime mutedUntil;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;
}