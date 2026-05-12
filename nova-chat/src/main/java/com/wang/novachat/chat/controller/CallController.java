package com.wang.novachat.chat.controller;

import com.wang.novachat.chat.entity.CallRecord;
import com.wang.novachat.chat.service.CallService;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/call")
@RequiredArgsConstructor
@Tag(name = "通话服务", description = "音视频通话记录查询接口")
public class CallController {

    private final CallService callService;

    @Operation(summary = "获取通话记录", description = "分页获取当前用户的通话记录列表")
    @GetMapping("/records")
    public Result<List<CallRecord>> getCallRecords(
            @Parameter(description = "页码,默认1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量,默认20") @RequestParam(required = false, defaultValue = "20") Integer size,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(callService.getCallRecords(userId, page, size));
    }

    @Operation(summary = "获取通话详情", description = "根据通话ID获取单条通话记录")
    @GetMapping("/records/{callId}")
    public Result<CallRecord> getCallRecord(
            @Parameter(description = "通话记录ID", required = true) @PathVariable Long callId,
            HttpServletRequest request) {
        requireUserId(request);
        return Result.success(callService.getCallById(callId));
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