package com.wang.novachat.chat.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MessageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long conversationId;

    private Long senderId;

    private Long receiverId;

    private String type;

    private String content;

    private String imageUrl;

    private String thumbUrl;

    private String originUrl;

    private Long quoteId;

    private Integer recalled;

    private LocalDateTime createTime;

    private QuoteMessageVO quoteMessage;

    @Data
    public static class QuoteMessageVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long id;
        private Long senderId;
        private String type;
        private String content;
        private String imageUrl;
        private String thumbUrl;
        private String originUrl;
    }
}
