package com.wang.novachat.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.entity.Conversation;
import com.wang.novachat.chat.entity.Message;
import com.wang.novachat.chat.mapper.ConversationMapper;
import com.wang.novachat.chat.mapper.MessageMapper;
import com.wang.novachat.chat.service.ChatService;
import com.wang.novachat.chat.vo.ConversationVO;
import com.wang.novachat.chat.vo.MessageVO;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public MessageVO sendMessage(Long senderId, SendMessageDTO dto) {
        Long conversationId = getOrCreateConversation(senderId, dto.getReceiverId());

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(dto.getReceiverId());
        message.setType(dto.getType());
        message.setContent(dto.getContent() != null ? dto.getContent() : "");
        message.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : "");
        message.setQuoteId(dto.getQuoteId());
        message.setRecalled(0);
        messageMapper.insert(message);

        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            String preview = buildPreview(dto);
            conv.setLastMessage(preview);
            conv.setLastMessageTime(LocalDateTime.now());
            if (conv.getUser1Id().equals(senderId)) {
                conv.setUnreadCountUser2(conv.getUnreadCountUser2() + 1);
            } else {
                conv.setUnreadCountUser1(conv.getUnreadCountUser1() + 1);
            }
            conversationMapper.updateById(conv);
        }

        return toMessageVO(message);
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUser1Id, userId)
                .or()
                .eq(Conversation::getUser2Id, userId)
                .orderByDesc(Conversation::getLastMessageTime);

        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        List<ConversationVO> result = new ArrayList<>();
        for (Conversation conv : conversations) {
            ConversationVO vo = new ConversationVO();
            vo.setId(conv.getId());

            boolean isUser1 = conv.getUser1Id().equals(userId);
            Long targetUserId = isUser1 ? conv.getUser2Id() : conv.getUser1Id();
            vo.setTargetUserId(targetUserId);
            vo.setUnreadCount(isUser1 ? conv.getUnreadCountUser1() : conv.getUnreadCountUser2());
            vo.setLastMessage(conv.getLastMessage());
            vo.setLastMessageTime(conv.getLastMessageTime());

            fillTargetUser(vo, targetUserId);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<MessageVO> getMessages(Long userId, Long conversationId, Long lastMsgId, Integer size) {
        if (size == null || size <= 0) size = 30;

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId);
        if (lastMsgId != null && lastMsgId > 0) {
            wrapper.lt(Message::getId, lastMsgId);
        }
        wrapper.orderByDesc(Message::getId);
        wrapper.last("LIMIT " + size);

        List<Message> messages = messageMapper.selectList(wrapper);

        return messages.stream()
                .sorted(Comparator.comparing(Message::getId))
                .map(this::toMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recallMessage(Long userId, Long messageId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在");
        }
        if (!msg.getSenderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能撤回自己的消息");
        }
        if (msg.getCreateTime().plusMinutes(2).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.FAIL, "超过2分钟，无法撤回");
        }
        msg.setRecalled(1);
        messageMapper.updateById(msg);
    }

    @Override
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        Message msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在");
        }
        messageMapper.deleteById(messageId);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long conversationId) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return;

        if (conv.getUser1Id().equals(userId)) {
            conv.setUnreadCountUser1(0);
        } else if (conv.getUser2Id().equals(userId)) {
            conv.setUnreadCountUser2(0);
        }
        conversationMapper.updateById(conv);
    }

    @Override
    @Transactional
    public Long getOrCreateConversation(Long user1Id, Long user2Id) {
        long minId = Math.min(user1Id, user2Id);
        long maxId = Math.max(user1Id, user2Id);
        String key = minId + "_" + maxId;

        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getConversationKey, key);
        Conversation conv = conversationMapper.selectOne(wrapper);

        if (conv != null) {
            return conv.getId();
        }

        conv = new Conversation();
        conv.setConversationKey(key);
        conv.setUser1Id(minId);
        conv.setUser2Id(maxId);
        conv.setLastMessage("");
        conv.setUnreadCountUser1(0);
        conv.setUnreadCountUser2(0);
        conversationMapper.insert(conv);

        return conv.getId();
    }

    private String buildPreview(SendMessageDTO dto) {
        return switch (dto.getType()) {
            case "image" -> "[图片]";
            case "emoji" -> "[表情]";
            default -> dto.getContent() != null && dto.getContent().length() > 50
                    ? dto.getContent().substring(0, 50) + "..."
                    : dto.getContent();
        };
    }

    private void fillTargetUser(ConversationVO vo, Long targetUserId) {
        try {
            String url = "http://nova-user/users/" + targetUserId;
            var response = restTemplate.getForObject(url, com.wang.novachat.common.result.Result.class, targetUserId);
            if (response != null && response.getData() != null) {
                var data = response.getData();
                if (data instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
                    vo.setTargetNickname((String) map.getOrDefault("nickname", "用户" + targetUserId));
                    vo.setTargetAvatar((String) map.getOrDefault("avatar", ""));
                }
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}: {}", targetUserId, e.getMessage());
            vo.setTargetNickname("用户" + targetUserId);
            vo.setTargetAvatar("");
        }
    }

    private MessageVO toMessageVO(Message msg) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(msg, vo);

        if (msg.getQuoteId() != null && msg.getQuoteId() > 0) {
            Message quoteMsg = messageMapper.selectById(msg.getQuoteId());
            if (quoteMsg != null) {
                MessageVO.QuoteMessageVO qvo = new MessageVO.QuoteMessageVO();
                qvo.setId(quoteMsg.getId());
                qvo.setSenderId(quoteMsg.getSenderId());
                qvo.setType(quoteMsg.getType());
                qvo.setContent(quoteMsg.getContent());
                qvo.setImageUrl(quoteMsg.getImageUrl());
                vo.setQuoteMessage(qvo);
            }
        }
        return vo;
    }
}
