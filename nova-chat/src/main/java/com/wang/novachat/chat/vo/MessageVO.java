package com.wang.novachat.chat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "消息详情")
@Data
public class MessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发送者用户ID")
    private Long senderId;

    @Schema(description = "接收者用户ID")
    private Long receiverId;

    @Schema(description = "消息类型: text/image/audio/video")
    private String type;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String thumbUrl;

    @Schema(description = "原图URL")
    private String originUrl;

    @Schema(description = "引用消息ID")
    private Long quoteId;

    @Schema(description = "是否已撤回: 0否 1是")
    private Integer recalled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "引用的消息")
    private QuoteMessageVO quoteMessage;

    @Schema(description = "引用消息详情")
    @Data
    public static class QuoteMessageVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "引用消息ID")
        private Long id;

        @Schema(description = "发送者用户ID")
        private Long senderId;

        @Schema(description = "消息类型")
        private String type;

        @Schema(description = "内容")
        private String content;

        @Schema(description = "图片URL")
        private String imageUrl;

        @Schema(description = "缩略图URL")
        private String thumbUrl;

        @Schema(description = "原图URL")
        private String originUrl;
    }
}
