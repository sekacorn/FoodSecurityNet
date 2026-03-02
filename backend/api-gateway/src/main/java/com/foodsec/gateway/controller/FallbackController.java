package com.foodsec.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller
 *
 * Provides fallback responses when circuit breakers are open
 * or when downstream services are unavailable.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.error("Auth service is unavailable - circuit breaker activated");
        return createFallbackResponse("Authentication service is temporarily unavailable");
    }

    @GetMapping("/agri-data")
    public ResponseEntity<Map<String, Object>> agriIntegratorFallback() {
        log.error("Agricultural data integrator is unavailable - circuit breaker activated");
        return createFallbackResponse("Agricultural data service is temporarily unavailable");
    }

    @GetMapping("/visualizations")
    public ResponseEntity<Map<String, Object>> agriVisualizerFallback() {
        log.error("Agricultural visualizer is unavailable - circuit breaker activated");
        return createFallbackResponse("Visualization service is temporarily unavailable");
    }

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> userSessionFallback() {
        log.error("User session service is unavailable - circuit breaker activated");
        return createFallbackResponse("Session service is temporarily unavailable");
    }

    @GetMapping("/llm")
    public ResponseEntity<Map<String, Object>> llmServiceFallback() {
        log.error("LLM service is unavailable - circuit breaker activated");
        return createFallbackResponse("AI insight service is temporarily unavailable");
    }

    @GetMapping("/collaboration")
    public ResponseEntity<Map<String, Object>> collaborationServiceFallback() {
        log.error("Collaboration service is unavailable - circuit breaker activated");
        return createFallbackResponse("Collaboration service is temporarily unavailable");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("suggestion", "Please try again later. If the issue persists, contact support.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}
