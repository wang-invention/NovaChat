package com.wang.novachat.moment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "评论信息")
@Data
public class MomentCommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String nickname;

    @Schema(description = "评论用户名")
    private String username;

    @Schema(description = "评论用户头像")
    private String avatar;

    @Schema(description = "回复目标用户ID")
    private Long replyToUserId;

    @Schema(description = "回复目标用户昵称")
    private String replyToNickname;

    @Schema(description = "回复目标用户名")
    private String replyToUsername;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}