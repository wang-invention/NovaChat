package com.wang.novachat.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "统一响应结构")
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应码: 200成功,其他为错误码", example = "200")
    private Integer code;

    @Schema(description = "提示信息", example = "操作成功")
    private String message;

    @Schema(description = "业务数据")
    private T data;

    @Schema(description = "响应时间戳(毫秒)", example = "1714034400000")
    private Long timestamp;

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(ResultCode resultCode, T data) {
        this(resultCode.getCode(), resultCode.getMessage(), data);
    }

    // ---------- success ----------

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ---------- fail ----------

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.FAIL, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.FAIL.getCode(), message, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    // ---------- helper ----------

    public boolean isSuccess() {
        return this.code != null && this.code.equals(ResultCode.SUCCESS.getCode());
    }
}
