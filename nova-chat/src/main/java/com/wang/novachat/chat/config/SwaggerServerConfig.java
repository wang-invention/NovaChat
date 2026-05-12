package com.wang.novachat.chat.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerServerConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            openApi.getServers().clear();
            // 关键：这里必须是网关地址 + 你的路由前缀
            openApi.addServersItem(new Server().url("http://129.211.0.210:8080/api/user"));
        };
    }
}