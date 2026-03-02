package com.foodsec.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Global Exception Handler
 *
 * Handles all exceptions in the API Gateway and returns
 * consistent error responses to clients.
 */
@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("Error processing request: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex);
        String errorMessage = determineErrorMessage(ex);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorResponse = buildErrorResponse(status, errorMessage, exchange.getRequest().getPath().value());

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(errorResponse.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Determine HTTP status based on exception type
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getStatusCode();
        }

        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }

        if (ex instanceof io.jsonwebtoken.JwtException) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Determine error message based on exception
     */
    private String determineErrorMessage(Throwable ex) {
        if (ex.getMessage() != null) {
            return ex.getMessage();
        }
        return "An unexpected error occurred";
    }

    /**
     * Build JSON error response
     */
    private String buildErrorResponse(HttpStatus status, String message, String path) {
        return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                LocalDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                escapeJson(message),
                escapeJson(path)
        );
    }

    /**
     * Escape special characters in JSON
     */
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
