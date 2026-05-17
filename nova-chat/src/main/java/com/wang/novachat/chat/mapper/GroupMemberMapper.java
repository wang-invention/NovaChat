package com.wang.novachat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.novachat.chat.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
}