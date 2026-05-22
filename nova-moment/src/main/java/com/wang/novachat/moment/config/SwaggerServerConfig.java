package com.wang.novachat.moment.config;

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

    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NovaMoment 接口文档")
                        .version("1.0.0")
                        .description("朋友圈API文档"))
                .components(new Components()
                        .addSecuritySchemes(HEADER_AUTHORIZATION,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
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
            openApi.addServersItem(new Server().url("http://129.211.0.210:30087/api/moment"));
        };
    }
}