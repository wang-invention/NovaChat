package com.wang.novachat.chat.service;

import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.vo.ConversationVO;
import com.wang.novachat.chat.vo.MessageVO;

import java.util.List;

public interface ChatService {

    MessageVO sendMessage(Long senderId, SendMessageDTO dto);

    List<ConversationVO> getConversations(Long userId);

    List<MessageVO> getMessages(Long userId, Long conversationId, Long lastMsgId, Integer size);

    void recallMessage(Long userId, Long messageId);

    void deleteMessage(Long userId, Long messageId);

    void markRead(Long userId, Long conversationId);

    Long getOrCreateConversation(Long user1Id, Long user2Id);
}
