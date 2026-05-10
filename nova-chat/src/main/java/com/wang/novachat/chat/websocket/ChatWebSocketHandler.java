package com.wang.novachat.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.service.ChatService;
import com.wang.novachat.chat.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    private static final Map<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            ONLINE_USERS.put(userId, session);
            log.info("WebSocket connected: userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long senderId = extractUserId(session);
        if (senderId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        try {
            WsMessage wsMsg = objectMapper.readValue(textMessage.getPayload(), WsMessage.class);

            if ("chat".equals(wsMsg.getType())) {
                SendMessageDTO dto = new SendMessageDTO();
                dto.setReceiverId(wsMsg.getTo());
                dto.setType(wsMsg.getMsgType() != null ? wsMsg.getMsgType() : "text");
                dto.setContent(wsMsg.getContent());
                dto.setImageUrl(wsMsg.getImageUrl());
                dto.setThumbUrl(wsMsg.getThumbUrl());
                dto.setOriginUrl(wsMsg.getOriginUrl());
                dto.setQuoteId(wsMsg.getQuoteId());

                MessageVO saved = chatService.sendMessage(senderId, dto);

                pushToUser(senderId, new WsPushMessage("chat_sent", saved));
                pushToUser(wsMsg.getTo(), new WsPushMessage("chat_received", saved));
            } else if ("recall".equals(wsMsg.getType())) {
                chatService.recallMessage(senderId, wsMsg.getMessageId());
                WsPushMessage push = new WsPushMessage("recalled", Map.of("messageId", wsMsg.getMessageId()));
                pushToUser(senderId, push);
            } else if ("read".equals(wsMsg.getType())) {
                chatService.markRead(senderId, wsMsg.getConversationId());
            }
        } catch (Exception e) {
            log.error("WebSocket handle message error: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = extractUserId(session);
        log.error("WebSocket transport error: userId={}, error={}", userId, exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public boolean pushToUser(Long userId, Object data) {
        WebSocketSession session = ONLINE_USERS.get(userId);
        if (session == null || !session.isOpen()) {
            return false;
        }
        try {
            String json = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(json));
            return true;
        } catch (IOException e) {
            log.error("Push to user failed: userId={}", userId, e);
            return false;
        }
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = ONLINE_USERS.get(userId);
        return session != null && session.isOpen();
    }

    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if ("userId".equals(kv[0]) && kv.length == 2) {
                try {
                    return Long.parseLong(kv[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @lombok.Data
    public static class WsMessage {
        private String type;
        private Long to;
        private String content;
        private String msgType;
        private String imageUrl;
        private String thumbUrl;
        private String originUrl;
        private Long quoteId;
        private Long messageId;
        private Long conversationId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class WsPushMessage {
        private String type;
        private Object data;
    }
}
