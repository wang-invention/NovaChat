package com.wang.novachat.user.controller;

import com.wang.novachat.common.exception.BusinessException;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${server.port:0}")
    private Integer port;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        log.info("[{}] health ping", applicationName);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", applicationName);
        data.put("port", port);
        data.put("status", "UP");
        data.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return Result.success(data);
    }

    /**
     * 用于联调全局异常 —— 请求 {@code /health/boom?type=biz|sys} 会抛对应异常。
     */
    @GetMapping("/boom")
    public Result<Void> boom(@RequestParam(defaultValue = "biz") String type) {
        if ("biz".equalsIgnoreCase(type)) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        throw new RuntimeException("unexpected system error for test");
    }
}
