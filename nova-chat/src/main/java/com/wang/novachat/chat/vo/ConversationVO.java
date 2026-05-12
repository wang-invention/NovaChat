package com.wang.novachat.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "会话信息")
@Data
public class ConversationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "对方用户ID")
    private Long targetUserId;

    @Schema(description = "对方昵称")
    private String targetNickname;

    @Schema(description = "对方头像URL")
    private String targetAvatar;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数量")
    private Integer unreadCount;
}
