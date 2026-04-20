package com.wang.novachat.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class HealthRouterConfig {

    @Value("${spring.application.name:nova-gateway}")
    private String applicationName;

    @Bean
    public RouterFunction<ServerResponse> healthRouter() {
        return RouterFunctions.route(GET("/health/ping"), request -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("app", applicationName);
            data.put("status", "UP");
            data.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(data);
        });
    }
}
