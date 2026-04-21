package com.wang.novachat.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * ID 生成工具。基于 Hutool，统一项目内生成策略。
 * <p>雪花 ID 的 workerId / dataCenterId 可后续接到配置中心（按服务实例 ID 派发）。
 */
public final class IdUtils {

    /** workerId / dataCenterId 先用默认值，后续可替换为基于 IP / 实例号的派发 */
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1L, 1L);

    private IdUtils() {
    }

    /** 32 位无短横线 UUID */
    public static String simpleUUID() {
        return IdUtil.simpleUUID();
    }

    /** 36 位带短横线 UUID */
    public static String uuid() {
        return IdUtil.randomUUID();
    }

    /** 雪花 ID（long） */
    public static long snowflakeId() {
        return SNOWFLAKE.nextId();
    }

    /** 雪花 ID（字符串形式，避免前端 JS 精度丢失） */
    public static String snowflakeIdStr() {
        return SNOWFLAKE.nextIdStr();
    }
}
