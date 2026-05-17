package com.wang.novachat.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "群聊信息")
@Data
public class GroupVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "群ID")
    private Long id;

    @Schema(description = "群名称")
    private String name;

    @Schema(description = "群头像URL")
    private String avatar;

    @Schema(description = "群主用户ID")
    private Long ownerId;

    @Schema(description = "群主昵称")
    private String ownerNickname;

    @Schema(description = "群公告")
    private String announcement;

    @Schema(description = "最大成员数")
    private Integer maxMembers;

    @Schema(description = "当前成员数")
    private Integer memberCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}