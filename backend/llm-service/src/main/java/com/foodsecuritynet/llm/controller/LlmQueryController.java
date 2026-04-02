package com.foodsecuritynet.llm.controller;

import com.foodsecuritynet.llm.model.LlmResponse;
import com.foodsecuritynet.llm.model.QueryContext;
import com.foodsecuritynet.llm.service.LlmIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LlmQueryController {

    private final LlmIntegrationService llmIntegrationService;

    @PostMapping("/query")
    public Mono<ResponseEntity<Map<String, Object>>> query(@RequestBody @Valid Map<String, Object> request) {
        log.info("Received LLM query request");

        try {
            String query = request.get("query").toString();
            String userId = request.getOrDefault("userId", "anonymous").toString();
            Map<String, Object> context = (Map<String, Object>) request.getOrDefault("context", Map.of());

            QueryContext queryContext = QueryContext.builder()
                    .query(query)
                    .userId(userId)
                    .context(context)
                    .build();

            return llmIntegrationService.processQuery(queryContext)
                    .map(response -> ResponseEntity.ok(Map.of(
                            "status", "success",
                            "response", response
                    )))
                    .onErrorResume(e -> {
                        log.error("Error processing query", e);
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to process query: " + e.getMessage())));
                    });

        } catch (Exception e) {
            log.error("Error in query endpoint", e);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request: " + e.getMessage())));
        }
    }

    @PostMapping("/analyze")
    public Mono<ResponseEntity<Map<String, Object>>> analyzeData(@RequestBody Map<String, Object> request) {
        log.info("Received data analysis request");

        try {
            String analysisType = request.get("analysisType").toString();
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            String userId = request.getOrDefault("userId", "anonymous").toString();

            return llmIntegrationService.analyzeData(analysisType, data, userId)
                    .map(response -> ResponseEntity.ok(Map.of(
                            "status", "success",
                            "analysis", response
                    )))
                    .onErrorResume(e -> {
                        log.error("Error analyzing data", e);
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Failed to analyze data: " + e.getMessage())));
                    });

        } catch (Exception e) {
            log.error("Error in analyze endpoint", e);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request: " + e.getMessage())));
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<Map<String, Object>> getQueryHistory(@PathVariable String userId) {
        log.info("Fetching query history for user: {}", userId);

        try {
            var history = llmIntegrationService.getQueryHistory(userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "history", history,
                    "count", history.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching query history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch history: " + e.getMessage()));
        }
    }
}
