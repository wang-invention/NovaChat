package com.wang.novachat.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.novachat.chat.dto.AddFriendDTO;
import com.wang.novachat.chat.entity.Friend;
import com.wang.novachat.chat.entity.FriendRequest;
import com.wang.novachat.chat.mapper.FriendMapper;
import com.wang.novachat.chat.mapper.FriendRequestMapper;
import com.wang.novachat.chat.service.FriendService;
import com.wang.novachat.chat.vo.FriendRequestVO;
import com.wang.novachat.chat.vo.FriendVO;
import com.wang.novachat.chat.websocket.ChatWebSocketHandler;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendMapper friendMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final RestTemplate restTemplate;
    private final ChatWebSocketHandler webSocketHandler;

    @Override
    public void sendRequest(Long userId, AddFriendDTO dto) {
        Long targetUserId = dto.getTargetUserId();

        if (userId.equals(targetUserId)) {
            throw new BusinessException(ResultCode.FAIL, "不能添加自己为好友");
        }

        if (isFriend(userId, targetUserId)) {
            throw new BusinessException(ResultCode.FAIL, "已经是好友了");
        }

        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getFromUserId, userId)
                .eq(FriendRequest::getToUserId, targetUserId)
                .eq(FriendRequest::getStatus, FriendRequest.STATUS_PENDING);
        FriendRequest existing = friendRequestMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ResultCode.FAIL, "已发送过申请，请等待对方处理");
        }

        LambdaQueryWrapper<FriendRequest> reverseWrapper = new LambdaQueryWrapper<>();
        reverseWrapper.eq(FriendRequest::getFromUserId, targetUserId)
                .eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, FriendRequest.STATUS_PENDING);
        FriendRequest reverseExisting = friendRequestMapper.selectOne(reverseWrapper);
        if (reverseExisting != null) {
            throw new BusinessException(ResultCode.FAIL, "对方已向你发送过申请，请先处理");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(userId);
        request.setToUserId(targetUserId);
        request.setMessage(dto.getMessage() != null ? dto.getMessage() : "我是你的好友");
        request.setStatus(FriendRequest.STATUS_PENDING);
        friendRequestMapper.insert(request);

        try {
            java.util.HashMap<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("requestId", request.getId());
            pushData.put("fromUserId", userId);
            pushData.put("message", request.getMessage());
            webSocketHandler.pushToUser(dto.getTargetUserId(),
                    new ChatWebSocketHandler.WsPushMessage("friend_request", pushData));
        } catch (Exception e) {
            log.warn("WebSocket push failed: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void acceptRequest(Long userId, Long requestId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "申请不存在");
        }
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权处理此申请");
        }
        if (request.getStatus() != FriendRequest.STATUS_PENDING) {
            throw new BusinessException(ResultCode.FAIL, "申请已处理");
        }

        request.setStatus(FriendRequest.STATUS_ACCEPTED);
        friendRequestMapper.updateById(request);

        addFriendRelation(request.getFromUserId(), request.getToUserId());

        try {
            java.util.HashMap<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("friendId", request.getToUserId());
            pushData.put("requestId", requestId);
            webSocketHandler.pushToUser(request.getFromUserId(),
                    new ChatWebSocketHandler.WsPushMessage("friend_accepted", pushData));
        } catch (Exception e) {
            log.warn("WebSocket push failed: {}", e.getMessage());
        }
    }

    @Override
    public void rejectRequest(Long userId, Long requestId) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "申请不存在");
        }
        if (!request.getToUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权处理此申请");
        }
        if (request.getStatus() != FriendRequest.STATUS_PENDING) {
            throw new BusinessException(ResultCode.FAIL, "申请已处理");
        }

        request.setStatus(FriendRequest.STATUS_REJECTED);
        friendRequestMapper.updateById(request);
    }

    @Override
    public List<FriendRequestVO> getPendingRequests(Long userId) {
        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, FriendRequest.STATUS_PENDING)
                .orderByDesc(FriendRequest::getCreateTime);
        List<FriendRequest> requests = friendRequestMapper.selectList(wrapper);

        List<FriendRequestVO> result = new ArrayList<>();
        for (FriendRequest req : requests) {
            FriendRequestVO vo = new FriendRequestVO();
            BeanUtils.copyProperties(req, vo);
            fillUserInfo(vo, req.getFromUserId());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<FriendVO> getFriendList(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .orderByAsc(Friend::getCreateTime);
        List<Friend> friends = friendMapper.selectList(wrapper);

        List<FriendVO> result = new ArrayList<>();
        for (Friend friend : friends) {
            FriendVO vo = new FriendVO();
            vo.setFriendId(friend.getFriendId());
            vo.setRemark(friend.getRemark());
            vo.setAddTime(friend.getCreateTime());
            fillFriendUserInfo(vo, friend.getFriendId());
            result.add(vo);
        }
        return result;
    }

    @Override
    public boolean isFriend(Long userId, Long targetUserId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, targetUserId);
        return friendMapper.selectCount(wrapper) > 0;
    }

    private void addFriendRelation(Long user1Id, Long user2Id) {
        Friend f1 = new Friend();
        f1.setUserId(user1Id);
        f1.setFriendId(user2Id);
        friendMapper.insert(f1);

        Friend f2 = new Friend();
        f2.setUserId(user2Id);
        f2.setFriendId(user1Id);
        friendMapper.insert(f2);
    }

    private void fillUserInfo(FriendRequestVO vo, Long userId) {
        try {
            String url = "http://nova-user/users/" + userId;
            var response = restTemplate.getForObject(url, com.wang.novachat.common.result.Result.class, userId);
            if (response != null && response.getData() != null && response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) response.getData();
                vo.setFromNickname((String) map.getOrDefault("nickname", "用户" + userId));
                vo.setFromAvatar((String) map.getOrDefault("avatar", ""));
                vo.setFromUsername((String) map.getOrDefault("username", ""));
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}: {}", userId, e.getMessage());
            vo.setFromNickname("用户" + userId);
        }
    }

    private void fillFriendUserInfo(FriendVO vo, Long friendId) {
        try {
            String url = "http://nova-user/users/" + friendId;
            var response = restTemplate.getForObject(url, com.wang.novachat.common.result.Result.class, friendId);
            if (response != null && response.getData() != null && response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) response.getData();
                vo.setNickname((String) map.getOrDefault("nickname", "用户" + friendId));
                vo.setAvatar((String) map.getOrDefault("avatar", ""));
                vo.setUsername((String) map.getOrDefault("username", ""));
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, friendId={}: {}", friendId, e.getMessage());
            vo.setNickname("用户" + friendId);
        }
    }
}
