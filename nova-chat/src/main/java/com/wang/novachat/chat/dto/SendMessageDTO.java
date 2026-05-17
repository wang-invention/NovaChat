package com.wang.novachat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "发送消息请求")
@Data
public class SendMessageDTO {

    @Schema(description = "接收者用户ID(私聊时必填)", example = "2")
    private Long receiverId;

    @Schema(description = "群ID(群聊时必填)", example = "1")
    private Long groupId;

    @Schema(description = "消息类型: text/text/image/audio/video", example = "text")
    @NotBlank(message = "消息类型不能为空")
    private String type;

    @Schema(description = "文本消息内容", example = "你好")
    private String content;

    @Schema(description = "图片消息URL(完整URL)")
    private String imageUrl;

    @Schema(description = "图片缩略图URL")
    private String thumbUrl;

    @Schema(description = "原图URL")
    private String originUrl;

    @Schema(description = "引用消息ID(回复引用)")
    private Long quoteId;
}
