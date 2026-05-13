package com.wang.novachat.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerServerConfig {


    // 认证头名称（和你项目一致）
    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NovaChat 接口文档")
                        .version("1.0.0")
                        .description("聊天系统API文档"))

                // ====================== 核心：自动加 Bearer 前缀 ======================
                .components(new Components()
                        .addSecuritySchemes(HEADER_AUTHORIZATION,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)  // HTTP 模式
                                        .scheme("bearer")                // 自动加 Bearer
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name(HEADER_AUTHORIZATION)
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(HEADER_AUTHORIZATION));
    }


    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            openApi.getServers().clear();
            // 关键：这里必须是网关地址 + 你的路由前缀
            openApi.addServersItem(new Server().url("http://129.211.0.210:8080/api/user"));
        };
    }
}