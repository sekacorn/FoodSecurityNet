package com.foodsec.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Logging Filter
 *
 * Logs all incoming requests and outgoing responses with:
 * - Request method and path
 * - Response status
 * - Request duration
 * - User information (if authenticated)
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();

        // Log incoming request
        logRequest(request);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    Duration duration = Duration.between(startTime, Instant.now());
                    logResponse(request, response, duration);
                }));
    }

    /**
     * Log incoming request details
     */
    private void logRequest(ServerHttpRequest request) {
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String userId = request.getHeaders().getFirst("X-User-Id");
        String ipAddress = getClientIpAddress(request);

        if (userId != null) {
            log.info("Incoming request: {} {} - User: {} - IP: {}",
                    method, path, userId, ipAddress);
        } else {
            log.info("Incoming request: {} {} - IP: {}",
                    method, path, ipAddress);
        }
    }

    /**
     * Log response details
     */
    private void logResponse(ServerHttpRequest request, ServerHttpResponse response, Duration duration) {
        String method = request.getMethod().name();
        String path = request.getPath().value();
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        long durationMs = duration.toMillis();

        log.info("Response: {} {} - Status: {} - Duration: {}ms",
                method, path, statusCode, durationMs);

        // Warn on slow requests
        if (durationMs > 3000) {
            log.warn("Slow request detected: {} {} took {}ms", method, path, durationMs);
        }
    }

    /**
     * Extract client IP address
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

        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // Run last to capture full request cycle
    }
}
