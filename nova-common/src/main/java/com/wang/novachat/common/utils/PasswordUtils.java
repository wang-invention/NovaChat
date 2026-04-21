package com.wang.novachat.common.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密工具：基于 BCrypt（自带随机盐，同一明文每次加密结果不同）。
 * <p>
 * 对比使用：{@link #matches(String, String)}，绝不要用字符串 equals 去比较 hash。
 */
public final class PasswordUtils {

    /**
     * BCrypt 工作因子：值越大越慢越安全。
     * 10 是 BCrypt 默认值，单次加密约 100ms，足够抗彩虹表。
     */
    private static final int WORK_FACTOR = 10;

    private PasswordUtils() {
    }

    /**
     * 加密明文密码。返回 60 位 BCrypt hash（含 $2a$ 版本头 + 盐 + 摘要）。
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * 校验明文与密文是否匹配。任何一个为 null 直接返回 false。
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            // 密文格式不合法（比如老数据里是明文），直接返回 false
            return false;
        }
    }
}
