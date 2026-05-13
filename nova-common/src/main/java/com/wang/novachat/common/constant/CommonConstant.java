package com.wang.novachat.common.constant;

/**
 * 全局通用常量。
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /** HTTP 鉴权头1 */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** JWT Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 网关透传到下游服务的用户 ID 头 */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** 网关透传到下游服务的用户名头 */
    public static final String HEADER_USERNAME = "X-User-Name";

    /** 网关透传到下游服务的设备 ID 头 */
    public static final String HEADER_DEVICE_ID = "X-Device-Id";

    /** 客户端设备 ID 请求头 */
    public static final String HEADER_X_DEVICE_ID = "X-Device-Id";

    /** 默认字符集 */
    public static final String CHARSET_UTF8 = "UTF-8";

    /** 默认分页页码 */
    public static final long DEFAULT_PAGE_NUM = 1L;

    /** 默认分页大小 */
    public static final long DEFAULT_PAGE_SIZE = 10L;

    /** 分页大小上限，防止一次捞太多 */
    public static final long MAX_PAGE_SIZE = 200L;
}
