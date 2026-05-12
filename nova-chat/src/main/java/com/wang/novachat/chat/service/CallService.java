package com.wang.novachat.chat.service;

import com.wang.novachat.chat.entity.CallRecord;

import java.util.List;

public interface CallService {

    CallRecord initiateCall(Long callerId, Long calleeId);

    CallRecord acceptCall(Long callId, Long userId);

    CallRecord rejectCall(Long callId, Long userId);

    CallRecord hangupCall(Long callId, Long userId);

    CallRecord getCallById(Long callId);

    List<CallRecord> getCallRecords(Long userId, Integer page, Integer size);
}