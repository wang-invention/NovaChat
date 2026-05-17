package com.wang.novachat.chat.controller;

import com.wang.novachat.chat.dto.AddGroupMemberDTO;
import com.wang.novachat.chat.dto.CreateGroupDTO;
import com.wang.novachat.chat.dto.UpdateGroupDTO;
import com.wang.novachat.chat.service.GroupService;
import com.wang.novachat.chat.vo.GroupMemberVO;
import com.wang.novachat.chat.vo.GroupVO;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
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
@RequestMapping("/group")
@RequiredArgsConstructor
@Tag(name = "群聊服务", description = "群聊创建、管理、成员操作接口")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "创建群聊", description = "创建一个新群聊，创建者自动成为群主")
    @PostMapping("/create")
    public Result<GroupVO> createGroup(
            @Valid @RequestBody CreateGroupDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        GroupVO vo = groupService.createGroup(userId, dto);
        return Result.success("群聊创建成功", vo);
    }

    @Operation(summary = "更新群信息", description = "修改群名称、头像、公告等(群主/管理员)")
    @PutMapping("/{groupId}")
    public Result<GroupVO> updateGroup(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        GroupVO vo = groupService.updateGroup(userId, groupId, dto);
        return Result.success("更新成功", vo);
    }

    @Operation(summary = "获取群信息", description = "获取群聊详细信息")
    @GetMapping("/{groupId}")
    public Result<GroupVO> getGroup(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId) {
        return Result.success(groupService.getGroup(groupId));
    }

    @Operation(summary = "获取我的群列表", description = "获取当前用户加入的所有群聊")
    @GetMapping("/my")
    public Result<List<GroupVO>> getMyGroups(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return Result.success(groupService.getMyGroups(userId));
    }

    @Operation(summary = "获取群成员列表", description = "获取指定群的所有成员")
    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getGroupMembers(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId) {
        return Result.success(groupService.getGroupMembers(groupId));
    }

    @Operation(summary = "添加群成员", description = "邀请用户加入群聊")
    @PostMapping("/{groupId}/members")
    public Result<Void> addMembers(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMemberDTO dto,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        groupService.addMembers(userId, groupId, dto);
        return Result.success("添加成功", null);
    }

    @Operation(summary = "移除群成员", description = "将指定用户移出群聊(群主/管理员)")
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "要移除的用户ID", required = true) @PathVariable("userId") Long targetUserId,
            HttpServletRequest request) {
        Long operatorId = requireUserId(request);
        groupService.removeMember(operatorId, groupId, targetUserId);
        return Result.success("移除成功", null);
    }

    @Operation(summary = "退出群聊", description = "主动退出群聊(非群主)")
    @DeleteMapping("/{groupId}/leave")
    public Result<Void> leaveGroup(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        groupService.leaveGroup(userId, groupId);
        return Result.success("已退出群聊", null);
    }

    @Operation(summary = "修改成员角色", description = "设置/取消管理员，或转让群主(仅群主)")
    @PutMapping("/{groupId}/members/{userId}/role")
    public Result<Void> changeRole(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "目标用户ID", required = true) @PathVariable("userId") Long targetUserId,
            @Parameter(description = "角色: 0成员 1管理员 2群主(转让)", required = true) @RequestParam Integer role,
            HttpServletRequest request) {
        Long operatorId = requireUserId(request);
        groupService.changeRole(operatorId, groupId, targetUserId, role);
        return Result.success("角色修改成功", null);
    }

    @Operation(summary = "解散群聊", description = "解散群聊(仅群主)")
    @DeleteMapping("/{groupId}")
    public Result<Void> dismissGroup(
            @Parameter(description = "群ID", required = true) @PathVariable Long groupId,
            HttpServletRequest request) {
        Long userId = requireUserId(request);
        groupService.dismissGroup(userId, groupId);
        return Result.success("群聊已解散", null);
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