package com.wang.novachat.chat.controller;

import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.service.ChatService;
import com.wang.novachat.chat.vo.ConversationVO;
import com.wang.novachat.chat.vo.MessageVO;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public Result<MessageVO> sendMessage(@Valid @RequestBody SendMessageDTO dto,
                                         HttpServletRequest request) {
        Long userId = requireUserId(request);
        MessageVO vo = chatService.sendMessage(userId, dto);
        return Result.success("发送成功", vo);
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(chatService.getConversations(userId));
    }

    @GetMapping("/messages")
    public Result<List<MessageVO>> getMessages(
            @RequestParam Long conversationId,
            @RequestParam(required = false) Long lastMsgId,
            @RequestParam(required = false, defaultValue = "30") Integer size,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(chatService.getMessages(userId, conversationId, lastMsgId, size));
    }

    @PostMapping("/messages/{messageId}/recall")
    public Result<Void> recallMessage(@PathVariable Long messageId,
                                       HttpServletRequest request) {
        Long userId = requireUserId(request);
        chatService.recallMessage(userId, messageId);
        return Result.success("已撤回", null);
    }

    @DeleteMapping("/messages/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId,
                                       HttpServletRequest request) {
        Long userId = requireUserId(request);
        chatService.deleteMessage(userId, messageId);
        return Result.success("已删除", null);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public Result<Void> markRead(@PathVariable Long conversationId,
                                  HttpServletRequest request) {
        Long userId = requireUserId(request);
        chatService.markRead(userId, conversationId);
        return Result.success(null);
    }

    @GetMapping("/conversations/id")
    public Result<Long> getConversationId(@RequestParam Long targetUserId,
                                           HttpServletRequest request) {
        Long userId = requireUserId(request);
        Long convId = chatService.getOrCreateConversation(userId, targetUserId);
        return Result.success(convId);
    }

    private Long requireUserId(HttpServletRequest request) {
        String header = request.getHeader(CommonConstant.HEADER_USER_ID);
        if (header == null || header.isBlank()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录");
        }
        try {
            return Long.parseLong(header);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "非法用户ID");
        }
    }
}
