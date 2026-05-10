package com.wang.novachat.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageDTO {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息类型不能为空")
    private String type;

    private String content;

    private String imageUrl;

    private String thumbUrl;

    private String originUrl;

    private Long quoteId;
}
