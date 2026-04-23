package com.wang.novachat.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.novachat.common.constant.CommonConstant;
import com.wang.novachat.common.result.Result;
import com.wang.novachat.common.result.ResultCode;
import com.wang.novachat.common.security.JwtService;
import com.wang.novachat.gateway.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关统一鉴权：解析并校验 JWT，校验通过后把 {@code userId / username} 透传到下游服务。
 * <p>白名单路径直接放行，失败以 {@link Result} 形式写回 401。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!authProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhiteList(path)) {
            log.debug("[Auth] 白名单放行：{}", path);
            return chain.filter(exchange);
        }

        // 防御性丢弃：下游不允许客户端自己塞 X-User-Id
        ServerHttpRequest sanitized = request.mutate()
                .headers(h -> {
                    h.remove(CommonConstant.HEADER_USER_ID);
                    h.remove(CommonConstant.HEADER_USERNAME);
                })
                .build();

        String authHeader = sanitized.getHeaders().getFirst(CommonConstant.HEADER_AUTHORIZATION);
        String token = jwtService.extractToken(authHeader);
        if (token == null || token.isBlank()) {
            return writeUnauthorized(exchange, ResultCode.UNAUTHORIZED, "缺少 Token");
        }

        Claims claims;
        try {
            claims = jwtService.parseClaims(token);
        } catch (ExpiredJwtException e) {
            log.info("[Auth] Token 已过期：path={}", path);
            return writeUnauthorized(exchange, ResultCode.TOKEN_EXPIRED, "Token 已过期");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[Auth] Token 无效：path={}, err={}", path, e.getMessage());
            return writeUnauthorized(exchange, ResultCode.TOKEN_INVALID, "Token 无效");
        }

        Long userId = jwtService.getUserId(claims);
        String username = jwtService.getUsername(claims);
        if (userId == null) {
            return writeUnauthorized(exchange, ResultCode.TOKEN_INVALID, "Token 缺少用户信息");
        }

        ServerHttpRequest mutated = sanitized.mutate()
                .header(CommonConstant.HEADER_USER_ID, String.valueOf(userId))
                .header(CommonConstant.HEADER_USERNAME, username == null ? "" : username)
                .build();

        log.debug("[Auth] 鉴权通过：userId={}, path={}", userId, path);
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isWhiteList(String path) {
        List<String> whiteList = authProperties.getWhiteList();
        if (whiteList == null || whiteList.isEmpty()) {
            return false;
        }
        for (String pattern : whiteList) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange,
                                         ResultCode code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> body = Result.fail(code, message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":" + code.getCode() + ",\"message\":\"" + message + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 尽量靠前执行，保证路由匹配之前完成鉴权
        return -100;
    }
}
