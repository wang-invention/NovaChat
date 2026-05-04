package com.wang.novachat.chat.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FriendRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fromUserId;

    private String fromNickname;

    private String fromAvatar;

    private String fromUsername;

    private Long toUserId;

    private String message;

    private Integer status;

    private LocalDateTime createTime;
}
