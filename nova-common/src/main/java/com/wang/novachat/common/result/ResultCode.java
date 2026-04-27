package com.wang.novachat.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一业务状态码。
 * <p>
 * 分段约定：
 * <ul>
 *     <li>200           - 成功</li>
 *     <li>400 ~ 499     - 客户端错误（参数、鉴权、资源）</li>
 *     <li>500 ~ 599     - 服务端错误</li>
 *     <li>1000 ~ 1999   - 用户模块业务码</li>
 *     <li>2000 ~ 2999   - AI / 聊天模块业务码</li>
 *     <li>3000 ~ 3999   - 订单 / 支付模块业务码</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "账号或密码错误"),
    TOKEN_INVALID(1004, "Token 无效"),
    TOKEN_EXPIRED(1005, "Token 已过期"),
    TOKEN_REVOKED(1006, "Token 已失效"),
    OTHER_DEVICE_LOGIN(1007, "您的账号在其他设备登录"),
    ;

    private final Integer code;
    private final String message;
}
