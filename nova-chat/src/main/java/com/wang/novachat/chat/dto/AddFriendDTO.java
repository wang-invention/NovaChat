package com.wang.novachat.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFriendDTO {

    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;

    private String message;
}
