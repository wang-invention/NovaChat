package com.wang.novachat.moment.service.impl;

import com.wang.novachat.moment.entity.MomentLike;
import com.wang.novachat.moment.mapper.MomentLikeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncLikeSyncService {

    private static final String LIKE_SET_PREFIX = "like:set:";
    private static final String LIKE_COUNT_KEY = "like:count";

    private final StringRedisTemplate stringRedisTemplate;
    private final MomentLikeMapper momentLikeMapper;

    @Async
    public void syncLikeData(Long momentId) {
        try {
            String setKey = LIKE_SET_PREFIX + momentId;
            Set<String> userIds = stringRedisTemplate.opsForSet().members(setKey);

            momentLikeMapper.deleteByMomentId(momentId);

            if (userIds != null && !userIds.isEmpty()) {
                for (String uid : userIds) {
                    MomentLike like = new MomentLike();
                    like.setMomentId(momentId);
                    like.setUserId(Long.valueOf(uid));
                    momentLikeMapper.insert(like);
                }
            }

            String countStr = userIds != null ? String.valueOf(userIds.size()) : "0";
            stringRedisTemplate.opsForHash().put(LIKE_COUNT_KEY, momentId.toString(), countStr);
        } catch (Exception e) {
            log.error("同步点赞数据失败, momentId={}: {}", momentId, e.getMessage(), e);
        }
    }

    public void loadLikeToRedis(Long momentId) {
        try {
            String setKey = LIKE_SET_PREFIX + momentId;

            List<MomentLike> likes = momentLikeMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MomentLike>()
                            .eq(MomentLike::getMomentId, momentId)
            );

            stringRedisTemplate.delete(setKey);
            if (likes != null && !likes.isEmpty()) {
                String[] userIdStrs = likes.stream()
                        .map(l -> l.getUserId().toString())
                        .toArray(String[]::new);
                stringRedisTemplate.opsForSet().add(setKey, userIdStrs);
                stringRedisTemplate.opsForHash().put(LIKE_COUNT_KEY, momentId.toString(), String.valueOf(likes.size()));
            } else {
                stringRedisTemplate.opsForHash().put(LIKE_COUNT_KEY, momentId.toString(), "0");
            }
        } catch (Exception e) {
            log.error("加载点赞数据到Redis失败, momentId={}: {}", momentId, e.getMessage(), e);
        }
    }
}