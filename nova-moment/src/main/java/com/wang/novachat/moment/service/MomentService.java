package com.wang.novachat.moment.service;

import com.wang.novachat.moment.dto.MomentCommentDTO;
import com.wang.novachat.moment.dto.PublishMomentDTO;
import com.wang.novachat.moment.vo.MomentCommentVO;
import com.wang.novachat.moment.vo.MomentVO;

import java.util.List;

public interface MomentService {

    MomentVO publish(Long userId, PublishMomentDTO dto);

    void delete(Long userId, Long momentId);

    MomentVO like(Long userId, Long momentId);

    MomentVO unlike(Long userId, Long momentId);

    MomentCommentVO comment(Long userId, Long momentId, MomentCommentDTO dto);

    void deleteComment(Long userId, Long commentId);

    List<MomentVO> getTimeline(Long userId, Integer page, Integer size);

    MomentVO getMoment(Long userId, Long momentId);
}