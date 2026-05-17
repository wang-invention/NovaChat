package com.wang.novachat.chat.service;

import com.wang.novachat.chat.dto.AddGroupMemberDTO;
import com.wang.novachat.chat.dto.CreateGroupDTO;
import com.wang.novachat.chat.dto.UpdateGroupDTO;
import com.wang.novachat.chat.vo.GroupMemberVO;
import com.wang.novachat.chat.vo.GroupVO;

import java.util.List;

public interface GroupService {

    GroupVO createGroup(Long ownerId, CreateGroupDTO dto);

    GroupVO updateGroup(Long userId, Long groupId, UpdateGroupDTO dto);

    GroupVO getGroup(Long groupId);

    List<GroupVO> getMyGroups(Long userId);

    List<GroupMemberVO> getGroupMembers(Long groupId);

    void addMembers(Long operatorId, Long groupId, AddGroupMemberDTO dto);

    void removeMember(Long operatorId, Long groupId, Long targetUserId);

    void leaveGroup(Long userId, Long groupId);

    void changeRole(Long operatorId, Long groupId, Long targetUserId, Integer role);

    void dismissGroup(Long userId, Long groupId);
}