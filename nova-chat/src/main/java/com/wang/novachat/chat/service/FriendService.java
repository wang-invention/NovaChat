package com.wang.novachat.chat.service;

import com.wang.novachat.chat.dto.AddFriendDTO;
import com.wang.novachat.chat.vo.FriendRequestVO;
import com.wang.novachat.chat.vo.FriendVO;

import java.util.List;

public interface FriendService {

    void sendRequest(Long userId, AddFriendDTO dto);

    void acceptRequest(Long userId, Long requestId);

    void rejectRequest(Long userId, Long requestId);

    List<FriendRequestVO> getPendingRequests(Long userId);

    List<FriendRequestVO> getRequestHistory(Long userId);

    List<FriendVO> getFriendList(Long userId);

    boolean isFriend(Long userId, Long targetUserId);
}
