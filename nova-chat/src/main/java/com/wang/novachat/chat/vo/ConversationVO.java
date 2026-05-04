package com.wang.novachat.chat.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ConversationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long targetUserId;

    private String targetNickname;

    private String targetAvatar;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;
}
