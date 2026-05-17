package com.wang.novachat.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.entity.CallRecord;
import com.wang.novachat.chat.entity.GroupMember;
import com.wang.novachat.chat.mapper.GroupMemberMapper;
import com.wang.novachat.chat.service.CallService;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final CallService callService;
    private final ObjectMapper objectMapper;
    private final GroupMemberMapper groupMemberMapper;

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
                dto.setGroupId(wsMsg.getGroupId());
                dto.setType(wsMsg.getMsgType() != null ? wsMsg.getMsgType() : "text");
                dto.setContent(wsMsg.getContent());
                dto.setImageUrl(wsMsg.getImageUrl());
                dto.setThumbUrl(wsMsg.getThumbUrl());
                dto.setOriginUrl(wsMsg.getOriginUrl());
                dto.setQuoteId(wsMsg.getQuoteId());

                MessageVO saved = chatService.sendMessage(senderId, dto);

                if (wsMsg.getGroupId() != null) {
                    pushToUser(senderId, new WsPushMessage("chat_sent", saved));
                    pushToGroup(wsMsg.getGroupId(), new WsPushMessage("chat_received", saved));
                } else {
                    pushToUser(senderId, new WsPushMessage("chat_sent", saved));
                    pushToUser(wsMsg.getTo(), new WsPushMessage("chat_received", saved));
                }
            } else if ("recall".equals(wsMsg.getType())) {
                chatService.recallMessage(senderId, wsMsg.getMessageId());
                WsPushMessage push = new WsPushMessage("recalled", Map.of("messageId", wsMsg.getMessageId()));
                pushToUser(senderId, push);
            } else if ("read".equals(wsMsg.getType())) {
                chatService.markRead(senderId, wsMsg.getConversationId());
            } else if ("call".equals(wsMsg.getType())) {
                handleCallSignal(senderId, wsMsg);
            } else if ("accept".equals(wsMsg.getType())) {
                handleCallSignal(senderId, wsMsg);
            } else if ("reject".equals(wsMsg.getType())) {
                handleCallSignal(senderId, wsMsg);
            } else if ("hangup".equals(wsMsg.getType())) {
                handleCallSignal(senderId, wsMsg);
            } else if ("sdp".equals(wsMsg.getType()) || "ice".equals(wsMsg.getType())) {
                handleCallSignal(senderId, wsMsg);
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

    private void pushToGroup(Long groupId, Object data) {
        List<GroupMember> members = groupMemberMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId));
        for (GroupMember member : members) {
            pushToUser(member.getUserId(), data);
        }
    }

    private void handleCallSignal(Long senderId, WsMessage wsMsg) {
        String signalType = wsMsg.getType();
        Long targetUserId = wsMsg.getTo();
        Long callId = wsMsg.getCallId();

        try {
            switch (signalType) {
                case "call" -> {
                    if (!isOnline(targetUserId)) {
                        pushToUser(senderId, new WsPushMessage("call_busy",
                                Map.of("message", "对方不在线")));
                        return;
                    }
                    CallRecord record = callService.initiateCall(senderId, targetUserId);
                    WsPushMessage incoming = new WsPushMessage("call_incoming", Map.of(
                            "callId", record.getId(),
                            "callerId", senderId,
                            "callType", "audio"
                    ));
                    pushToUser(targetUserId, incoming);
                    pushToUser(senderId, new WsPushMessage("call_ringing", Map.of(
                            "callId", record.getId()
                    )));
                }
                case "accept" -> {
                    CallRecord record = callService.acceptCall(callId, senderId);
                    Long peerId = record.getCallerId().equals(senderId)
                            ? record.getCalleeId() : record.getCallerId();
                    pushToUser(peerId, new WsPushMessage("call_accepted", Map.of(
                            "callId", callId
                    )));
                }
                case "reject" -> {
                    CallRecord record = callService.rejectCall(callId, senderId);
                    Long peerId = record.getCallerId().equals(senderId)
                            ? record.getCalleeId() : record.getCallerId();
                    pushToUser(peerId, new WsPushMessage("call_rejected", Map.of(
                            "callId", callId
                    )));
                }
                case "hangup" -> {
                    CallRecord record = callService.hangupCall(callId, senderId);
                    Long peerId = record.getCallerId().equals(senderId)
                            ? record.getCalleeId() : record.getCallerId();
                    pushToUser(peerId, new WsPushMessage("call_hangup", Map.of(
                            "callId", callId,
                            "duration", record.getDuration()
                    )));
                    pushToUser(senderId, new WsPushMessage("call_ended", Map.of(
                            "callId", callId,
                            "duration", record.getDuration()
                    )));
                }
                case "sdp" -> {
                    pushToUser(targetUserId, new WsPushMessage("call_sdp", Map.of(
                            "callId", callId,
                            "sdp", wsMsg.getSdp(),
                            "from", senderId
                    )));
                }
                case "ice" -> {
                    pushToUser(targetUserId, new WsPushMessage("call_ice", Map.of(
                            "callId", callId,
                            "candidate", wsMsg.getCandidate(),
                            "sdpMid", wsMsg.getSdpMid(),
                            "sdpMLineIndex", wsMsg.getSdpMLineIndex(),
                            "from", senderId
                    )));
                }
            }
        } catch (Exception e) {
            log.error("Call signal error: type={}, sender={}, error={}", signalType, senderId, e.getMessage());
            pushToUser(senderId, new WsPushMessage("call_error", Map.of(
                    "message", e.getMessage()
            )));
        }
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
        private Long groupId;
        private String content;
        private String msgType;
        private String imageUrl;
        private String thumbUrl;
        private String originUrl;
        private Long quoteId;
        private Long messageId;
        private Long conversationId;
        private Long callId;
        private String sdp;
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class WsPushMessage {
        private String type;
        private Object data;
    }
}
