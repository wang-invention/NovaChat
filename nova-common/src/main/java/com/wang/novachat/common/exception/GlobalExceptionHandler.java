package com.wang.novachat.common.exception;

import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 * <p>仅在引入 spring-webmvc（即 classpath 存在 {@link DispatcherServlet}）时加载。
 * WebFlux 网关不会加载本类，避免 ClassNotFoundException。
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnClass(DispatcherServlet.class)
public class GlobalExceptionHandler {

    /**
     * 业务异常：按抛出的 code / message 原样返回。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * @RequestBody 上的 @Valid 校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ":" + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[参数校验] {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR, msg);
    }

    /**
     * 表单 / Query 参数绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("[参数绑定] {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR, msg);
    }

    /**
     * @RequestParam / @PathVariable 上的约束校验失败（方法形参注解）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[约束校验] {}", msg);
        return Result.fail(ResultCode.PARAM_ERROR, msg);
    }

    /**
     * 必传参数缺失。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("[缺少参数] {}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, "缺少必填参数：" + e.getParameterName());
    }

    /**
     * 请求体解析失败（JSON 格式错误等）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[请求体解析失败] {}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, "请求体格式错误");
    }

    /**
     * 请求方法不支持（如用 GET 访问 POST 接口）。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[方法不支持] {}", e.getMessage());
        return Result.fail(ResultCode.METHOD_NOT_ALLOWED, e.getMessage());
    }

    /**
     * 404：需要在 application.yml 中开启
     * {@code spring.mvc.throw-exception-if-no-handler-found=true}
     * 并关闭静态资源映射，才会走到这里。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNotFound(NoHandlerFoundException e) {
        log.warn("[404] {}", e.getMessage());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底：未预期的异常。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("[系统异常] ", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }
}
