package com.wang.novachat.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.novachat.chat.entity.CallRecord;
import com.wang.novachat.chat.mapper.CallRecordMapper;
import com.wang.novachat.chat.service.CallService;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallServiceImpl implements CallService {

    private final CallRecordMapper callRecordMapper;

    @Override
    @Transactional
    public CallRecord initiateCall(Long callerId, Long calleeId) {
        if (callerId.equals(calleeId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能给自己打电话");
        }

        CallRecord record = new CallRecord();
        record.setCallerId(callerId);
        record.setCalleeId(calleeId);
        record.setStatus("calling");
        record.setCallType("audio");
        record.setDuration(0);
        callRecordMapper.insert(record);
        log.info("Call initiated: callId={}, caller={}, callee={}", record.getId(), callerId, calleeId);
        return record;
    }

    @Override
    @Transactional
    public CallRecord acceptCall(Long callId, Long userId) {
        CallRecord record = getCallById(callId);
        if (!record.getCalleeId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此通话");
        }
        if (!"calling".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "通话状态异常，无法接听");
        }
        record.setStatus("ongoing");
        record.setStartTime(LocalDateTime.now());
        callRecordMapper.updateById(record);
        log.info("Call accepted: callId={}", callId);
        return record;
    }

    @Override
    @Transactional
    public CallRecord rejectCall(Long callId, Long userId) {
        CallRecord record = getCallById(callId);
        if (!record.getCalleeId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此通话");
        }
        if (!"calling".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "通话状态异常，无法拒绝");
        }
        record.setStatus("rejected");
        record.setEndTime(LocalDateTime.now());
        callRecordMapper.updateById(record);
        log.info("Call rejected: callId={}", callId);
        return record;
    }

    @Override
    @Transactional
    public CallRecord hangupCall(Long callId, Long userId) {
        CallRecord record = getCallById(callId);
        if (!record.getCallerId().equals(userId) && !record.getCalleeId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此通话");
        }

        String currentStatus = record.getStatus();
        if ("ended".equals(currentStatus) || "rejected".equals(currentStatus)) {
            return record;
        }

        record.setEndTime(LocalDateTime.now());

        if ("ongoing".equals(currentStatus) && record.getStartTime() != null) {
            long seconds = Duration.between(record.getStartTime(), record.getEndTime()).getSeconds();
            record.setDuration((int) seconds);
        }

        if ("calling".equals(currentStatus)) {
            record.setStatus("missed");
        } else {
            record.setStatus("ended");
        }

        callRecordMapper.updateById(record);
        log.info("Call hung up: callId={}, duration={}s", callId, record.getDuration());
        return record;
    }

    @Override
    public CallRecord getCallById(Long callId) {
        CallRecord record = callRecordMapper.selectById(callId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通话记录不存在");
        }
        return record;
    }

    @Override
    public List<CallRecord> getCallRecords(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<CallRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(CallRecord::getCallerId, userId).or().eq(CallRecord::getCalleeId, userId))
               .orderByDesc(CallRecord::getCreateTime);
        Page<CallRecord> pageResult = callRecordMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 20), wrapper);
        return pageResult.getRecords();
    }
}