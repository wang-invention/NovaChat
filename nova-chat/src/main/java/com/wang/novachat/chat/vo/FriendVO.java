package com.wang.novachat.chat.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FriendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long friendId;

    private String nickname;

    private String username;

    private String avatar;

    private String remark;

    private LocalDateTime addTime;
}
