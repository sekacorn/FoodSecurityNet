package com.foodsec.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * Rate Limiting Filter
 *
 * Implements token bucket algorithm using Redis to limit
 * the number of requests per user/IP address.
 *
 * Limits:
 * - Authenticated users: 100 requests per minute
 * - Anonymous users: 20 requests per minute
 * - LLM endpoints: 10 requests per minute (higher cost)
 */
@Slf4j
@Component
public class RateLimitingFilter implements GatewayFilter, Ordered {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final int AUTHENTICATED_USER_LIMIT = 100;
    private static final int ANONYMOUS_USER_LIMIT = 20;
    private static final int LLM_ENDPOINT_LIMIT = 10;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Determine rate limit key and limit
        String rateLimitKey = getRateLimitKey(exchange);
        int limit = getRateLimit(exchange);

        // Increment request count in Redis
        return redisTemplate.opsForValue()
                .increment(rateLimitKey)
                .flatMap(requestCount -> {
                    // Set expiration on first request
                    if (requestCount == 1) {
                        return redisTemplate.expire(rateLimitKey, WINDOW_DURATION)
                                .then(processRequest(exchange, chain, requestCount, limit));
                    }
                    return processRequest(exchange, chain, requestCount, limit);
                })
                .onErrorResume(e -> {
                    log.error("Redis error during rate limiting: {}", e.getMessage());
                    // On Redis failure, allow the request (fail open)
                    return chain.filter(exchange);
                });
    }

    /**
     * Process the request based on rate limit
     */
    private Mono<Void> processRequest(ServerWebExchange exchange, GatewayFilterChain chain,
                                       Long requestCount, int limit) {
        ServerHttpResponse response = exchange.getResponse();

        // Add rate limit headers
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(limit));
        response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - requestCount)));

        if (requestCount > limit) {
            log.warn("Rate limit exceeded for key: {}, count: {}, limit: {}",
                    getRateLimitKey(exchange), requestCount, limit);
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
            response.getHeaders().add("Retry-After", "60");

            String errorBody = String.format(
                    "{\"error\":\"Rate limit exceeded\",\"limit\":%d,\"retryAfter\":60}",
                    limit
            );
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorBody.getBytes())));
        }

        log.debug("Rate limit check passed: {}/{} for key: {}",
                requestCount, limit, getRateLimitKey(exchange));
        return chain.filter(exchange);
    }

    /**
     * Generate rate limit key based on user or IP
     */
    private String getRateLimitKey(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        // Use user ID if authenticated
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "rate_limit:user:" + userId;
        }

        // Fall back to IP address for anonymous users
        String ipAddress = getClientIpAddress(request);
        return "rate_limit:ip:" + ipAddress;
    }

    /**
     * Determine rate limit based on endpoint and authentication status
     */
    private int getRateLimit(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Lower limit for LLM endpoints
        if (path.startsWith("/api/llm") || path.startsWith("/api/insights")) {
            return LLM_ENDPOINT_LIMIT;
        }

        // Check if user is authenticated
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return AUTHENTICATED_USER_LIMIT;
        }

        return ANONYMOUS_USER_LIMIT;
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
    }

    @Override
    public int getOrder() {
        return -50; // Run after JWT filter but before routing
    }
}
