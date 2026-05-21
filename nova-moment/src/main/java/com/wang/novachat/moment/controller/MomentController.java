package com.wang.novachat.moment.controller;

import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.moment.dto.MomentCommentDTO;
import com.wang.novachat.moment.dto.PublishMomentDTO;
import com.wang.novachat.moment.service.MomentService;
import com.wang.novachat.moment.vo.MomentCommentVO;
import com.wang.novachat.moment.vo.MomentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/moment")
@RequiredArgsConstructor
@Tag(name = "朋友圈服务", description = "朋友圈发布、点赞、评论、动态流接口")
public class MomentController {

    private final MomentService momentService;


    @Operation(summary = "发布朋友圈")
    @PostMapping
    public Result<MomentVO> publish(
            @Valid @RequestBody PublishMomentDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success("发布成功", momentService.publish(userId, dto));
    }

    @Operation(summary = "删除朋友圈")
    @DeleteMapping("/{momentId}")
    public Result<Void> delete(
            @Parameter(description = "动态ID", required = true) @PathVariable Long momentId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        momentService.delete(userId, momentId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "点赞")
    @PostMapping("/{momentId}/like")
    public Result<MomentVO> like(
            @Parameter(description = "动态ID", required = true) @PathVariable Long momentId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(momentService.like(userId, momentId));
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{momentId}/like")
    public Result<MomentVO> unlike(
            @Parameter(description = "动态ID", required = true) @PathVariable Long momentId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(momentService.unlike(userId, momentId));
    }

    @Operation(summary = "评论")
    @PostMapping("/{momentId}/comment")
    public Result<MomentCommentVO> comment(
            @Parameter(description = "动态ID", required = true) @PathVariable Long momentId,
            @Valid @RequestBody MomentCommentDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success("评论成功", momentService.comment(userId, momentId, dto));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        momentService.deleteComment(userId, commentId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "朋友圈动态流")
    @GetMapping("/timeline")
    public Result<List<MomentVO>> getTimeline(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(momentService.getTimeline(userId, page, size));
    }

    @Operation(summary = "获取单条动态")
    @GetMapping("/{momentId}")
    public Result<MomentVO> getMoment(
            @Parameter(description = "动态ID", required = true) @PathVariable Long momentId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(momentService.getMoment(userId, momentId));
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