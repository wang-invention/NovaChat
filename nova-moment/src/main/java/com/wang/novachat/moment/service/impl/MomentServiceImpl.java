package com.wang.novachat.moment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.moment.dto.MomentCommentDTO;
import com.wang.novachat.moment.dto.PublishMomentDTO;
import com.wang.novachat.moment.entity.Moment;
import com.wang.novachat.moment.entity.MomentComment;
import com.wang.novachat.moment.entity.MomentImage;
import com.wang.novachat.moment.mapper.MomentCommentMapper;
import com.wang.novachat.moment.mapper.MomentImageMapper;
import com.wang.novachat.moment.mapper.MomentMapper;
import com.wang.novachat.moment.service.MomentService;
import com.wang.novachat.moment.vo.MomentCommentVO;
import com.wang.novachat.moment.vo.MomentLikeVO;
import com.wang.novachat.moment.vo.MomentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomentServiceImpl implements MomentService {

    private static final String LIKE_SET_PREFIX = "like:set:";
    private static final String LIKE_COUNT_KEY = "like:count";

    private final MomentMapper momentMapper;
    private final MomentImageMapper momentImageMapper;
    private final MomentCommentMapper momentCommentMapper;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final AsyncLikeSyncService asyncLikeSyncService;

    @Override
    @Transactional
    public MomentVO publish(Long userId, PublishMomentDTO dto) {
        Moment moment = new Moment();
        moment.setUserId(userId);
        moment.setContent(dto.getContent());
        momentMapper.insert(moment);

        if (!CollectionUtils.isEmpty(dto.getImages())) {
            int order = 0;
            for (String url : dto.getImages()) {
                MomentImage img = new MomentImage();
                img.setMomentId(moment.getId());
                img.setImageUrl(url);
                img.setSortOrder(order++);
                momentImageMapper.insert(img);
            }
        }

        return buildMomentVO(moment, userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long momentId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null || !moment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的动态");
        }
        momentMapper.deleteById(momentId);
    }

    @Override
    @Transactional
    public MomentVO like(Long userId, Long momentId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }

        String setKey = LIKE_SET_PREFIX + momentId;
        String uid = userId.toString();

        loadLikeFromMysqlIfAbsent(momentId, setKey);

        Boolean alreadyLiked = stringRedisTemplate.opsForSet().isMember(setKey, uid);
        if (Boolean.TRUE.equals(alreadyLiked)) {
            return buildMomentVO(moment, userId);
        }

        stringRedisTemplate.opsForSet().add(setKey, uid);
        stringRedisTemplate.opsForHash().increment(LIKE_COUNT_KEY, momentId.toString(), 1);

        asyncLikeSyncService.syncLikeData(momentId);

        return buildMomentVO(moment, userId);
    }

    @Override
    @Transactional
    public MomentVO unlike(Long userId, Long momentId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }

        String setKey = LIKE_SET_PREFIX + momentId;
        String uid = userId.toString();

        loadLikeFromMysqlIfAbsent(momentId, setKey);

        Boolean alreadyLiked = stringRedisTemplate.opsForSet().isMember(setKey, uid);
        if (!Boolean.TRUE.equals(alreadyLiked)) {
            return buildMomentVO(moment, userId);
        }

        stringRedisTemplate.opsForSet().remove(setKey, uid);
        stringRedisTemplate.opsForHash().increment(LIKE_COUNT_KEY, momentId.toString(), -1);

        asyncLikeSyncService.syncLikeData(momentId);

        return buildMomentVO(moment, userId);
    }

    @Override
    @Transactional
    public MomentCommentVO comment(Long userId, Long momentId, MomentCommentDTO dto) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }

        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        momentCommentMapper.insert(comment);

        return buildCommentVO(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        MomentComment comment = momentCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        Moment moment = momentMapper.selectById(comment.getMomentId());
        if (moment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
        if (!comment.getUserId().equals(userId) && !moment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评论或自己动态下的评论");
        }
        momentCommentMapper.deleteById(commentId);
    }

    @Override
    public List<MomentVO> getTimeline(Long userId, Integer page, Integer size) {
        List<Long> friendIds = getFriendIds(userId);
        friendIds.add(userId);

        LambdaQueryWrapper<Moment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Moment::getUserId, friendIds)
                .orderByDesc(Moment::getCreateTime);

        Page<Moment> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 10);
        Page<Moment> result = momentMapper.selectPage(pageParam, wrapper);

        return result.getRecords().stream()
                .map(m -> buildMomentVO(m, userId))
                .collect(Collectors.toList());
    }

    @Override
    public MomentVO getMoment(Long userId, Long momentId) {
        Moment moment = momentMapper.selectById(momentId);
        if (moment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
        return buildMomentVO(moment, userId);
    }

    private void loadLikeFromMysqlIfAbsent(Long momentId, String setKey) {
        Boolean exists = stringRedisTemplate.hasKey(setKey);
        if (!Boolean.TRUE.equals(exists)) {
            asyncLikeSyncService.loadLikeToRedis(momentId);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> getFriendIds(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(CommonConstant.HEADER_USER_ID, String.valueOf(userId));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    "http://nova-chat/friend/list",
                    HttpMethod.GET,
                    entity,
                    com.wang.novachat.common.result.Result.class);
            if (response.getBody() != null && response.getBody().getData() != null) {
                List<Map<String, Object>> friends = (List<Map<String, Object>>) response.getBody().getData();
                return friends.stream()
                        .map(f -> Long.valueOf(f.get("friendId").toString()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("获取好友列表失败, userId={}: {}", userId, e.getMessage());
        }
        return new ArrayList<>();
    }

    private MomentVO buildMomentVO(Moment moment, Long currentUserId) {
        MomentVO vo = new MomentVO();
        vo.setId(moment.getId());
        vo.setUserId(moment.getUserId());
        vo.setContent(moment.getContent());
        vo.setCreateTime(moment.getCreateTime());

        fillUserInfo(vo, moment.getUserId());

        List<String> imageUrls = momentImageMapper.selectList(
                new LambdaQueryWrapper<MomentImage>()
                        .eq(MomentImage::getMomentId, moment.getId())
                        .orderByAsc(MomentImage::getSortOrder))
                .stream().map(MomentImage::getImageUrl).collect(Collectors.toList());
        vo.setImages(imageUrls);

        fillLikesFromRedis(vo, moment.getId(), currentUserId);

        List<MomentComment> comments = momentCommentMapper.selectList(
                new LambdaQueryWrapper<MomentComment>()
                        .eq(MomentComment::getMomentId, moment.getId())
                        .orderByAsc(MomentComment::getCreateTime));
        List<MomentCommentVO> commentVOs = comments.stream().map(this::buildCommentVO).collect(Collectors.toList());
        vo.setComments(commentVOs);
        vo.setCommentCount(commentVOs.size());

        return vo;
    }

    private void fillLikesFromRedis(MomentVO vo, Long momentId, Long currentUserId) {
        try {
            String setKey = LIKE_SET_PREFIX + momentId;
            loadLikeFromMysqlIfAbsent(momentId, setKey);

            Set<String> userIds = stringRedisTemplate.opsForSet().members(setKey);
            if (userIds == null || userIds.isEmpty()) {
                vo.setLikes(Collections.emptyList());
                vo.setLikeCount(0);
                vo.setLiked(false);
                return;
            }

            List<MomentLikeVO> likeVOs = new ArrayList<>();
            boolean liked = false;
            String currentUid = currentUserId.toString();

            for (String uid : userIds) {
                Long userId = Long.valueOf(uid);
                MomentLikeVO lvo = new MomentLikeVO();
                lvo.setUserId(userId);
                fillLikeUserInfo(lvo, userId);
                likeVOs.add(lvo);
                if (currentUid.equals(uid)) {
                    liked = true;
                }
            }

            vo.setLikes(likeVOs);
            vo.setLikeCount(likeVOs.size());
            vo.setLiked(liked);
        } catch (Exception e) {
            log.warn("从Redis获取点赞数据失败, momentId={}: {}", momentId, e.getMessage());
            vo.setLikes(Collections.emptyList());
            vo.setLikeCount(0);
            vo.setLiked(false);
        }
    }

    private MomentCommentVO buildCommentVO(MomentComment comment) {
        MomentCommentVO vo = new MomentCommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());

        fillCommentUserInfo(vo, comment.getUserId());
        if (comment.getReplyToUserId() != null) {
            fillReplyUserInfo(vo, comment.getReplyToUserId());
        }
        return vo;
    }

    private void fillUserInfo(MomentVO vo, Long userId) {
        try {
            Map<String, Object> map = fetchUserInfo(userId);
            vo.setNickname((String) map.getOrDefault("nickname", "用户" + userId));
            vo.setUsername((String) map.getOrDefault("username", ""));
            vo.setAvatar((String) map.getOrDefault("avatar", ""));
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}: {}", userId, e.getMessage());
            vo.setNickname("用户" + userId);
        }
    }

    private void fillLikeUserInfo(MomentLikeVO vo, Long userId) {
        try {
            Map<String, Object> map = fetchUserInfo(userId);
            vo.setNickname((String) map.getOrDefault("nickname", "用户" + userId));
            vo.setUsername((String) map.getOrDefault("username", ""));
        } catch (Exception e) {
            vo.setNickname("用户" + userId);
        }
    }

    private void fillCommentUserInfo(MomentCommentVO vo, Long userId) {
        try {
            Map<String, Object> map = fetchUserInfo(userId);
            vo.setNickname((String) map.getOrDefault("nickname", "用户" + userId));
            vo.setUsername((String) map.getOrDefault("username", ""));
            vo.setAvatar((String) map.getOrDefault("avatar", ""));
        } catch (Exception e) {
            vo.setNickname("用户" + userId);
        }
    }

    private void fillReplyUserInfo(MomentCommentVO vo, Long userId) {
        try {
            Map<String, Object> map = fetchUserInfo(userId);
            vo.setReplyToNickname((String) map.getOrDefault("nickname", "用户" + userId));
            vo.setReplyToUsername((String) map.getOrDefault("username", ""));
        } catch (Exception e) {
            vo.setReplyToNickname("用户" + userId);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchUserInfo(Long userId) {
        String url = "http://nova-user/users/" + userId;
        var response = restTemplate.getForObject(url, com.wang.novachat.common.result.Result.class, userId);
        if (response != null && response.getData() != null) {
            return (Map<String, Object>) response.getData();
        }
        return Collections.emptyMap();
    }
}