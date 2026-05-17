package com.wang.novachat.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.novachat.chat.dto.AddGroupMemberDTO;
import com.wang.novachat.chat.dto.CreateGroupDTO;
import com.wang.novachat.chat.dto.UpdateGroupDTO;
import com.wang.novachat.chat.entity.Conversation;
import com.wang.novachat.chat.entity.Group;
import com.wang.novachat.chat.entity.GroupMember;
import com.wang.novachat.chat.mapper.ConversationMapper;
import com.wang.novachat.chat.mapper.GroupMapper;
import com.wang.novachat.chat.mapper.GroupMemberMapper;
import com.wang.novachat.chat.service.GroupService;
import com.wang.novachat.chat.vo.GroupMemberVO;
import com.wang.novachat.chat.vo.GroupVO;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final ConversationMapper conversationMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public GroupVO createGroup(Long ownerId, CreateGroupDTO dto) {
        Group group = new Group();
        group.setName(dto.getName());
        group.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "");
        group.setOwnerId(ownerId);
        group.setAnnouncement("");
        group.setMaxMembers(dto.getMaxMembers() != null ? dto.getMaxMembers() : 200);
        groupMapper.insert(group);

        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(GroupMember.ROLE_OWNER);
        groupMemberMapper.insert(ownerMember);

        for (Long memberId : dto.getMemberIds()) {
            if (memberId.equals(ownerId)) continue;
            GroupMember member = new GroupMember();
            member.setGroupId(group.getId());
            member.setUserId(memberId);
            member.setRole(GroupMember.ROLE_MEMBER);
            groupMemberMapper.insert(member);
        }

        Conversation conv = new Conversation();
        conv.setConversationKey("group_" + group.getId());
        conv.setUser1Id(group.getId());
        conv.setUser2Id(0L);
        conv.setLastMessage("");
        conv.setUnreadCountUser1(0);
        conv.setUnreadCountUser2(0);
        conversationMapper.insert(conv);

        return toGroupVO(group);
    }

    @Override
    @Transactional
    public GroupVO updateGroup(Long userId, Long groupId, UpdateGroupDTO dto) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        checkAdmin(group, userId);

        if (dto.getName() != null) group.setName(dto.getName());
        if (dto.getAvatar() != null) group.setAvatar(dto.getAvatar());
        if (dto.getAnnouncement() != null) group.setAnnouncement(dto.getAnnouncement());
        if (dto.getMaxMembers() != null) group.setMaxMembers(dto.getMaxMembers());
        groupMapper.updateById(group);

        return toGroupVO(group);
    }

    @Override
    public GroupVO getGroup(Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        return toGroupVO(group);
    }

    @Override
    public List<GroupVO> getMyGroups(Long userId) {
        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getUserId, userId);
        List<GroupMember> memberships = groupMemberMapper.selectList(memberWrapper);

        List<GroupVO> result = new ArrayList<>();
        for (GroupMember member : memberships) {
            Group group = groupMapper.selectById(member.getGroupId());
            if (group != null) {
                result.add(toGroupVO(group));
            }
        }
        return result;
    }

    @Override
    public List<GroupMemberVO> getGroupMembers(Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .orderByAsc(GroupMember::getRole);
        List<GroupMember> members = groupMemberMapper.selectList(wrapper);

        List<GroupMemberVO> result = new ArrayList<>();
        for (GroupMember member : members) {
            GroupMemberVO vo = new GroupMemberVO();
            vo.setId(member.getId());
            vo.setUserId(member.getUserId());
            vo.setRole(member.getRole());
            vo.setNicknameInGroup(member.getNicknameInGroup());
            vo.setMutedUntil(member.getMutedUntil());
            vo.setJoinTime(member.getCreateTime());
            fillUserInfo(vo, member.getUserId());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public void addMembers(Long operatorId, Long groupId, AddGroupMemberDTO dto) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        checkMember(group, operatorId);

        long currentCount = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, groupId));
        if (currentCount + dto.getUserIds().size() > group.getMaxMembers()) {
            throw new BusinessException(ResultCode.FAIL, "群成员已达上限");
        }

        for (Long userId : dto.getUserIds()) {
            Long exists = groupMemberMapper.selectCount(
                    new LambdaQueryWrapper<GroupMember>()
                            .eq(GroupMember::getGroupId, groupId)
                            .eq(GroupMember::getUserId, userId));
            if (exists > 0) continue;

            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole(GroupMember.ROLE_MEMBER);
            groupMemberMapper.insert(member);
        }
    }

    @Override
    @Transactional
    public void removeMember(Long operatorId, Long groupId, Long targetUserId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        if (targetUserId.equals(group.getOwnerId())) {
            throw new BusinessException(ResultCode.FAIL, "不能移除群主");
        }
        checkAdmin(group, operatorId);

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, targetUserId);
        groupMemberMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        if (userId.equals(group.getOwnerId())) {
            throw new BusinessException(ResultCode.FAIL, "群主不能退群，请先转让群主或解散群");
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void changeRole(Long operatorId, Long groupId, Long targetUserId, Integer role) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        if (!operatorId.equals(group.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有群主可以修改角色");
        }
        if (targetUserId.equals(group.getOwnerId())) {
            throw new BusinessException(ResultCode.FAIL, "不能修改群主的角色");
        }
        if (role == GroupMember.ROLE_OWNER) {
            group.setOwnerId(targetUserId);
            groupMapper.updateById(group);

            LambdaQueryWrapper<GroupMember> oldOwnerWrapper = new LambdaQueryWrapper<>();
            oldOwnerWrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, operatorId);
            GroupMember oldOwner = groupMemberMapper.selectOne(oldOwnerWrapper);
            if (oldOwner != null) {
                oldOwner.setRole(GroupMember.ROLE_ADMIN);
                groupMemberMapper.updateById(oldOwner);
            }
        }

        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, targetUserId);
        GroupMember member = groupMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该用户不在群内");
        }
        member.setRole(role);
        groupMemberMapper.updateById(member);
    }

    @Override
    @Transactional
    public void dismissGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群不存在");
        }
        if (!userId.equals(group.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有群主可以解散群");
        }

        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getGroupId, groupId);
        groupMemberMapper.delete(memberWrapper);

        LambdaQueryWrapper<Conversation> convWrapper = new LambdaQueryWrapper<>();
        convWrapper.eq(Conversation::getConversationKey, "group_" + groupId);
        conversationMapper.delete(convWrapper);

        groupMapper.deleteById(groupId);
    }

    private GroupVO toGroupVO(Group group) {
        GroupVO vo = new GroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setAvatar(group.getAvatar());
        vo.setOwnerId(group.getOwnerId());
        vo.setAnnouncement(group.getAnnouncement());
        vo.setMaxMembers(group.getMaxMembers());
        vo.setCreateTime(group.getCreateTime());

        long count = groupMemberMapper.selectCount(
                new LambdaQueryWrapper<GroupMember>().eq(GroupMember::getGroupId, group.getId()));
        vo.setMemberCount((int) count);

        fillOwnerInfo(vo, group.getOwnerId());
        return vo;
    }

    private void checkAdmin(Group group, Long userId) {
        if (!userId.equals(group.getOwnerId())) {
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, group.getId())
                    .eq(GroupMember::getUserId, userId)
                    .eq(GroupMember::getRole, GroupMember.ROLE_ADMIN);
            if (groupMemberMapper.selectCount(wrapper) == 0) {
                throw new BusinessException(ResultCode.FORBIDDEN, "只有群主和管理员可以执行此操作");
            }
        }
    }

    private void checkMember(Group group, Long userId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, group.getId())
                .eq(GroupMember::getUserId, userId);
        if (groupMemberMapper.selectCount(wrapper) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "你不是该群成员");
        }
    }

    private void fillOwnerInfo(GroupVO vo, Long ownerId) {
        try {
            String url = "http://nova-user/users/" + ownerId;
            var response = restTemplate.getForObject(url, com.wang.novachat.common.result.Result.class, ownerId);
            if (response != null && response.getData() != null && response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) response.getData();
                vo.setOwnerNickname((String) map.getOrDefault("nickname", "用户" + ownerId));
            }
        } catch (Exception e) {
            log.warn("获取群主信息失败, ownerId={}: {}", ownerId, e.getMessage());
            vo.setOwnerNickname("用户" + ownerId);
        }
    }

    private void fillUserInfo(GroupMemberVO vo, Long userId) {
        try {
            String url = "http://nova-user/users/" + userId;
            var response = restTemplate.getForObject(url, com.wang.novachat.common.result.Result.class, userId);
            if (response != null && response.getData() != null && response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) response.getData();
                vo.setNickname((String) map.getOrDefault("nickname", "用户" + userId));
                vo.setAvatar((String) map.getOrDefault("avatar", ""));
                vo.setUsername((String) map.getOrDefault("username", ""));
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}: {}", userId, e.getMessage());
            vo.setNickname("用户" + userId);
        }
    }
}