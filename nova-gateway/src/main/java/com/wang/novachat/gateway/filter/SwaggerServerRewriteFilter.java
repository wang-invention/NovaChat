package com.wang.novachat.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerServerRewriteFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 只处理 /v3/api-docs 请求
        if (!path.startsWith("/v3/api-docs")) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        // 合并所有 DataBuffer
                        DataBuffer joined = bufferFactory.join(dataBuffers);
                        byte[] content = new byte[joined.readableByteCount()];
                        joined.read(content);
                        DataBufferUtils.release(joined);

                        String responseBody = new String(content, StandardCharsets.UTF_8);

                        // 修改 servers 地址
                        String modifiedBody = rewriteServers(responseBody, exchange);

                        byte[] modifiedContent = modifiedBody.getBytes(StandardCharsets.UTF_8);
                        originalResponse.getHeaders().setContentLength(modifiedContent.length);
                        return bufferFactory.wrap(modifiedContent);
                    }));
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private String rewriteServers(String responseBody, ServerWebExchange exchange) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            if (root instanceof ObjectNode) {
                ObjectNode rootObj = (ObjectNode) root;
                ArrayNode servers = objectMapper.createArrayNode();

                // 获取网关地址
                String scheme = exchange.getRequest().getURI().getScheme();
                String host = exchange.getRequest().getURI().getHost();
                int port = exchange.getRequest().getURI().getPort();

                String gatewayUrl;
                if (port > 0 && port != 80 && port != 443) {
                    gatewayUrl = scheme + "://" + host + ":" + port;
                } else {
                    gatewayUrl = scheme + "://" + host;
                }

                // 根据请求路径添加对应的服务前缀
                String path = exchange.getRequest().getURI().getPath();
                String servicePrefix = getServicePrefix(path);
                if (servicePrefix != null) {
                    gatewayUrl = gatewayUrl + servicePrefix;
                }

                ObjectNode serverNode = objectMapper.createObjectNode();
                serverNode.put("url", gatewayUrl);
                serverNode.put("description", "网关地址");
                servers.add(serverNode);

                rootObj.set("servers", servers);

                return objectMapper.writeValueAsString(rootObj);
            }
        } catch (Exception e) {
            log.error("重写 Swagger servers 失败", e);
        }
        return responseBody;
    }

    private String getServicePrefix(String path) {
        if (path.contains("/v3/api-docs/user")) {
            return "/api/user";
        } else if (path.contains("/v3/api-docs/chat")) {
            return "/api/chat";
        }
        return null;
    }

    @Override
    public int getOrder() {
        // 在响应过滤器之后执行
        return -2;
    }
}
