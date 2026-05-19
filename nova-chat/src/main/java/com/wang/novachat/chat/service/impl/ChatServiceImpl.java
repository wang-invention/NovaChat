package com.wang.novachat.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.entity.Conversation;
import com.wang.novachat.chat.entity.ConversationMemberRead;
import com.wang.novachat.chat.entity.Group;
import com.wang.novachat.chat.entity.GroupMember;
import com.wang.novachat.chat.entity.Message;
import com.wang.novachat.chat.mapper.ConversationMapper;
import com.wang.novachat.chat.mapper.ConversationMemberReadMapper;
import com.wang.novachat.chat.mapper.GroupMapper;
import com.wang.novachat.chat.mapper.GroupMemberMapper;
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
    private final ConversationMemberReadMapper conversationMemberReadMapper;
    private final MessageMapper messageMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public MessageVO sendMessage(Long senderId, SendMessageDTO dto) {
        boolean isGroup = dto.getGroupId() != null;
        Long conversationId;

        if (isGroup) {
            conversationId = getGroupConversationId(dto.getGroupId());
        } else {
            conversationId = getOrCreateConversation(senderId, dto.getReceiverId());
        }

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(isGroup ? 0L : dto.getReceiverId());
        message.setType(dto.getType());
        message.setContent(dto.getContent() != null ? dto.getContent() : "");
        message.setImageUrl(dto.getThumbUrl() != null ? dto.getThumbUrl() : (dto.getImageUrl() != null ? dto.getImageUrl() : ""));
        message.setThumbUrl(dto.getThumbUrl() != null ? dto.getThumbUrl() : "");
        message.setOriginUrl(dto.getOriginUrl() != null ? dto.getOriginUrl() : "");
        message.setQuoteId(dto.getQuoteId());
        message.setRecalled(0);
        messageMapper.insert(message);
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            String preview = buildPreview(dto);
            if (isGroup) {
                log.info("群聊发消息，只更新lastMessage，conversationId={}", conversationId);
                LambdaUpdateWrapper<ConversationMemberRead> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(ConversationMemberRead::getConversationId, conversationId)
                        .ne(ConversationMemberRead::getUserId, senderId)
                        .setSql("unread_count = unread_count + 1");
                conversationMemberReadMapper.update(null, updateWrapper);

                LambdaUpdateWrapper<Conversation> convUpdate = new LambdaUpdateWrapper<>();
                convUpdate.eq(Conversation::getId, conversationId)
                        .set(Conversation::getLastMessage, preview)
                        .set(Conversation::getLastMessageTime, LocalDateTime.now());
                conversationMapper.update(null, convUpdate);
            } else {
                conv.setLastMessage(preview);
                conv.setLastMessageTime(LocalDateTime.now());
                if (conv.getUser1Id().equals(senderId)) {
                    conv.setUnreadCountUser2(conv.getUnreadCountUser2() + 1);
                } else {
                    conv.setUnreadCountUser1(conv.getUnreadCountUser1() + 1);
                }
                conversationMapper.updateById(conv);
            }
        }

        return toMessageVO(message);
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        List<ConversationVO> result = new ArrayList<>();

        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUser1Id, userId)
                .or()
                .eq(Conversation::getUser2Id, userId)
                .orderByDesc(Conversation::getLastMessageTime);
        List<Conversation> privateConvs = conversationMapper.selectList(wrapper);

        for (Conversation conv : privateConvs) {
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

        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getUserId, userId);
        List<GroupMember> memberships = groupMemberMapper.selectList(memberWrapper);

        for (GroupMember member : memberships) {
            String convKey = "group_" + member.getGroupId();
            LambdaQueryWrapper<Conversation> convWrapper = new LambdaQueryWrapper<>();
            convWrapper.eq(Conversation::getConversationKey, convKey);
            Conversation groupConv = conversationMapper.selectOne(convWrapper);

            if (groupConv != null) {
                ConversationVO vo = new ConversationVO();
                vo.setId(groupConv.getId());
                vo.setTargetUserId(0L);

                LambdaQueryWrapper<ConversationMemberRead> readWrapper = new LambdaQueryWrapper<>();
                readWrapper.eq(ConversationMemberRead::getConversationId, groupConv.getId())
                        .eq(ConversationMemberRead::getUserId, userId);
                ConversationMemberRead read = conversationMemberReadMapper.selectOne(readWrapper);
                vo.setUnreadCount(read != null ? read.getUnreadCount() : 0);

                vo.setLastMessage(groupConv.getLastMessage());
                vo.setLastMessageTime(groupConv.getLastMessageTime());

                Group group = groupMapper.selectById(member.getGroupId());
                if (group != null) {
                    vo.setConversationType("GROUP");
                    vo.setGroupId(group.getId());
                    vo.setGroupName(group.getName());
                    vo.setGroupAvatar(group.getAvatar());
                }
                result.add(vo);
            }
        }

        result.sort((a, b) -> {
            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
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

        boolean isGroup = conv.getConversationKey() != null && conv.getConversationKey().startsWith("group_");

        if (isGroup) {
            LambdaQueryWrapper<Message> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(Message::getConversationId, conversationId)
                    .orderByDesc(Message::getId)
                    .last("LIMIT 1");
            Message latestMsg = messageMapper.selectOne(msgWrapper);
            Long maxMsgId = latestMsg != null ? latestMsg.getId() : 0L;

            LambdaUpdateWrapper<ConversationMemberRead> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ConversationMemberRead::getConversationId, conversationId)
                    .eq(ConversationMemberRead::getUserId, userId)
                    .set(ConversationMemberRead::getUnreadCount, 0)
                    .set(ConversationMemberRead::getLastReadMessageId, maxMsgId);
            conversationMemberReadMapper.update(null, updateWrapper);
        } else {
            if (conv.getUser1Id().equals(userId)) {
                conv.setUnreadCountUser1(0);
            } else if (conv.getUser2Id().equals(userId)) {
                conv.setUnreadCountUser2(0);
            }
            conversationMapper.updateById(conv);
        }
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

    @Transactional
    public Long getGroupConversationId(Long groupId) {
        String key = "group_" + groupId;

        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getConversationKey, key);
        Conversation conv = conversationMapper.selectOne(wrapper);

        if (conv != null) {
            return conv.getId();
        }

        conv = new Conversation();
        conv.setConversationKey(key);
        conv.setUser1Id(groupId);
        conv.setUser2Id(0L);
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
