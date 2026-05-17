package com.wang.novachat.chat.controller;

import com.wang.novachat.chat.dto.AddFriendDTO;
import com.wang.novachat.chat.service.FriendService;
import com.wang.novachat.chat.vo.FriendRequestVO;
import com.wang.novachat.chat.vo.FriendVO;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
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
@RequestMapping("/friend")
@RequiredArgsConstructor
@Tag(name = "好友服务", description = "好友申请、添加、管理接口")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "发送好友申请", description = "向指定用户发送好友申请")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "申请已发送"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/request")
    public Result<Void> sendRequest(
            @Valid @RequestBody @Parameter(description = "好友申请信息", required = true) AddFriendDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        friendService.sendRequest(userId, dto);
        return Result.success("申请已发送", null);
    }

    @Operation(summary = "接受好友申请", description = "接受指定的好友申请")
    @PostMapping("/request/{requestId}/accept")
    public Result<Void> acceptRequest(
            @Parameter(description = "好友申请ID", required = true) @PathVariable Long requestId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        friendService.acceptRequest(userId, requestId);
        return Result.success("已添加好友", null);
    }

    @Operation(summary = "拒绝好友申请", description = "拒绝指定的好友申请")
    @PostMapping("/request/{requestId}/reject")
    public Result<Void> rejectRequest(
            @Parameter(description = "好友申请ID", required = true) @PathVariable Long requestId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        friendService.rejectRequest(userId, requestId);
        return Result.success("已拒绝", null);
    }

    @Operation(summary = "获取待处理申请", description = "获取当前用户收到的好友申请列表")
    @GetMapping("/requests/pending")
    public Result<List<FriendRequestVO>> getPendingRequests(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(friendService.getPendingRequests(userId));
    }

    @Operation(summary = "获取申请记录", description = "获取当前用户的所有好友申请历史记录(发出的+收到的)")
    @GetMapping("/requests/history")
    public Result<List<FriendRequestVO>> getRequestHistory(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(friendService.getRequestHistory(userId));
    }

    @Operation(summary = "获取好友列表", description = "获取当前用户的所有好友列表")
    @GetMapping("/list")
    public Result<List<FriendVO>> getFriendList(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(friendService.getFriendList(userId));
    }

    @Operation(summary = "检查是否好友", description = "检查当前用户与指定用户是否为好友关系")
    @GetMapping("/isFriend")
    public Result<Boolean> isFriend(
            @Parameter(description = "目标用户ID", required = true) @RequestParam("targetUserId") Long targetUserId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(friendService.isFriend(userId, targetUserId));
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
