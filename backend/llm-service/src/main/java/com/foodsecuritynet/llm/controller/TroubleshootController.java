package com.foodsecuritynet.llm.controller;

import com.foodsecuritynet.llm.service.ErrorAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/troubleshoot")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TroubleshootController {

    private final ErrorAnalysisService errorAnalysisService;

    @PostMapping("/analyze-error")
    public Mono<ResponseEntity<Map<String, Object>>> analyzeError(@RequestBody Map<String, Object> request) {
        log.info("Analyzing error for troubleshooting");

        try {
            String errorMessage = request.get("errorMessage").toString();
            String errorType = request.getOrDefault("errorType", "GENERAL").toString();
            Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", Map.of());

            return errorAnalysisService.analyzeError(errorMessage, errorType, context)
                    .map(analysis -> ResponseEntity.ok(Map.of(
                            "status", "success",
                            "analysis", analysis
                    )))
                    .onErrorResume(e -> {
                        log.error("Error analyzing error", e);
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to analyze error: " + e.getMessage())));
                    });

        } catch (Exception e) {
            log.error("Error in analyze-error endpoint", e);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request: " + e.getMessage())));
        }
    }

    @PostMapping("/suggest-fix")
    public Mono<ResponseEntity<Map<String, Object>>> suggestFix(@RequestBody Map<String, Object> request) {
        log.info("Suggesting fix for issue");

        try {
            String issue = request.get("issue").toString();
            Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", Map.of());

            return errorAnalysisService.suggestFix(issue, context)
                    .map(suggestions -> ResponseEntity.ok(Map.of(
                            "status", "success",
                            "suggestions", suggestions
                    )))
                    .onErrorResume(e -> {
                        log.error("Error suggesting fix", e);
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to suggest fix: " + e.getMessage())));
                    });

        } catch (Exception e) {
            log.error("Error in suggest-fix endpoint", e);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request: " + e.getMessage())));
        }
    }

    @GetMapping("/error-logs/{userId}")
    public ResponseEntity<Map<String, Object>> getErrorLogs(@PathVariable String userId) {
        log.info("Fetching error logs for user: {}", userId);

        try {
            var logs = errorAnalysisService.getErrorLogs(userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "logs", logs,
                    "count", logs.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching error logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch error logs: " + e.getMessage()));
        }
    }
}
