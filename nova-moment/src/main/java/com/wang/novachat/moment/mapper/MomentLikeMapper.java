package com.wang.novachat.moment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.novachat.moment.entity.MomentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MomentLikeMapper extends BaseMapper<MomentLike> {

    @Delete("DELETE FROM t_moment_like WHERE moment_id = #{momentId}")
    int deleteByMomentId(@Param("momentId") Long momentId);
}