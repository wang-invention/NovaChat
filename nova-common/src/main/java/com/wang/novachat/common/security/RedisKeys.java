package com.wang.novachat.common.security;

public final class RedisKeys {

    private RedisKeys() {
    }

    private static final String PREFIX = "user:login:";

    public static String session(Long userId, String deviceId) {
        return PREFIX + "session:" + userId + ":" + deviceId;
    }

    public static String token(String tokenId) {
        return PREFIX + "token:" + tokenId;
    }

    public static String devices(Long userId) {
        return PREFIX + "devices:" + userId;
    }
}