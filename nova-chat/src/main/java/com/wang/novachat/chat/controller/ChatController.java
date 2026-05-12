package com.wang.novachat.chat.controller;

import com.wang.novachat.chat.dto.SendMessageDTO;
import com.wang.novachat.chat.service.ChatService;
import com.wang.novachat.chat.vo.ConversationVO;
import com.wang.novachat.chat.vo.MessageVO;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "聊天服务", description = "消息发送、对话管理接口")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息", description = "向指定用户发送文本或图片消息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "不在会话中")
    })
    @PostMapping("/messages")
    public Result<MessageVO> sendMessage(
            @Valid @RequestBody @Parameter(description = "消息内容", required = true) SendMessageDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        MessageVO vo = chatService.sendMessage(userId, dto);
        return Result.success("发送成功", vo);
    }

    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话列表，按最新消息时间倒序")
    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(chatService.getConversations(userId));
    }

    @Operation(summary = "获取消息列表", description = "获取指定会话的消息历史，支持分页")
    @GetMapping("/messages")
    public Result<List<MessageVO>> getMessages(
            @Parameter(description = "会话ID", required = true) @RequestParam Long conversationId,
            @Parameter(description = "上次获取的最后一条消息ID(用于分页)") @RequestParam(required = false) Long lastMsgId,
            @Parameter(description = "每页数量,默认30") @RequestParam(required = false, defaultValue = "30") Integer size,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(chatService.getMessages(userId, conversationId, lastMsgId, size));
    }

    @Operation(summary = "撤回消息", description = "撤回已发送的消息，只能撤回自己发送的消息")
    @PostMapping("/messages/{messageId}/recall")
    public Result<Void> recallMessage(
            @Parameter(description = "消息ID", required = true) @PathVariable Long messageId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        chatService.recallMessage(userId, messageId);
        return Result.success("已撤回", null);
    }

    @Operation(summary = "删除消息", description = "删除消息，接收方将看不到该消息")
    @DeleteMapping("/messages/{messageId}")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID", required = true) @PathVariable Long messageId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        chatService.deleteMessage(userId, messageId);
        return Result.success("已删除", null);
    }

    @Operation(summary = "标记已读", description = "将指定会话的所有消息标记为已读")
    @PostMapping("/conversations/{conversationId}/read")
    public Result<Void> markRead(
            @Parameter(description = "会话ID", required = true) @PathVariable Long conversationId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        chatService.markRead(userId, conversationId);
        return Result.success(null);
    }

    @Operation(summary = "获取或创建会话", description = "根据目标用户ID获取或创建一个私聊会话")
    @GetMapping("/conversations/id")
    public Result<Long> getConversationId(
            @Parameter(description = "目标用户ID", required = true) @RequestParam Long targetUserId,
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
