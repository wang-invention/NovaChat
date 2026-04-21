package com.wang.novachat.common.exception;

import com.wang.novachat.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常：被 {@link GlobalExceptionHandler} 捕获后，直接以 {@code code + message} 返回。
 * <p>业务代码中推荐直接 {@code throw new BusinessException(ResultCode.XXX)}。
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }
}
