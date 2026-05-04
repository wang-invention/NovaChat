package com.wang.novachat.chat.controller;

import com.wang.novachat.chat.dto.AddFriendDTO;
import com.wang.novachat.chat.service.FriendService;
import com.wang.novachat.chat.vo.FriendRequestVO;
import com.wang.novachat.chat.vo.FriendVO;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
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
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public Result<Void> sendRequest(@Valid @RequestBody AddFriendDTO dto,
                                     HttpServletRequest request) {
        Long userId = requireUserId(request);
        friendService.sendRequest(userId, dto);
        return Result.success("申请已发送", null);
    }

    @PostMapping("/request/{requestId}/accept")
    public Result<Void> acceptRequest(@PathVariable Long requestId,
                                       HttpServletRequest request) {
        Long userId = requireUserId(request);
        friendService.acceptRequest(userId, requestId);
        return Result.success("已添加好友", null);
    }

    @PostMapping("/request/{requestId}/reject")
    public Result<Void> rejectRequest(@PathVariable Long requestId,
                                       HttpServletRequest request) {
        Long userId = requireUserId(request);
        friendService.rejectRequest(userId, requestId);
        return Result.success("已拒绝", null);
    }

    @GetMapping("/requests/pending")
    public Result<List<FriendRequestVO>> getPendingRequests(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(friendService.getPendingRequests(userId));
    }

    @GetMapping("/list")
    public Result<List<FriendVO>> getFriendList(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(friendService.getFriendList(userId));
    }

    @GetMapping("/isFriend")
    public Result<Boolean> isFriend(@RequestParam("targetUserId") Long targetUserId,
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
