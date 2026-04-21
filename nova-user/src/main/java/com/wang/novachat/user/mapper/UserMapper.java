package com.wang.novachat.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.novachat.user.entity.User;

/**
 * 用户 Mapper。基础 CRUD 走 MP，复杂 SQL 另写 XML 到 resources/mapper 下。
 */
public interface UserMapper extends BaseMapper<User> {
}
