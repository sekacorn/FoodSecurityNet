package com.foodsec.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Adds defensive response headers at the gateway boundary.
 */
@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = exchange.getResponse().getHeaders();

        headers.setIfAbsent("X-Content-Type-Options", "nosniff");
        headers.setIfAbsent("X-Frame-Options", "DENY");
        headers.setIfAbsent("Referrer-Policy", "strict-origin-when-cross-origin");
        headers.setIfAbsent("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=(), usb=()");
        headers.setIfAbsent("Cache-Control", "no-store");

        if ("https".equalsIgnoreCase(request.getURI().getScheme())) {
            headers.setIfAbsent("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
