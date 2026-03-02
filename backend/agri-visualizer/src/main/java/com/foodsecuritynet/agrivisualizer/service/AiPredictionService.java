package com.foodsecuritynet.agrivisualizer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for calling AI model predictions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPredictionService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.llm-service.url:http://llm-service:8085}")
    private String llmServiceUrl;

    /**
     * Request AI prediction for visualization data
     */
    public Mono<Map<String, Object>> requestPrediction(Long visualizationId, Map<String, Object> data) {
        log.info("Requesting AI prediction for visualization: {}", visualizationId);

        Map<String, Object> request = new HashMap<>();
        request.put("visualizationId", visualizationId);
        request.put("data", data);
        request.put("predictionType", "visualization_enhancement");

        return webClientBuilder.build()
                .post()
                .uri(llmServiceUrl + "/api/llm/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("AI prediction received for visualization: {}", visualizationId))
                .doOnError(error -> log.error("Error requesting AI prediction: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Failed to get AI prediction, returning empty result", error);
                    return Mono.just(new HashMap<>());
                });
    }

    /**
     * Request crop yield prediction
     */
    public Mono<Map<String, Object>> predictCropYield(Map<String, Object> agriculturalData) {
        log.info("Requesting crop yield prediction");

        Map<String, Object> request = new HashMap<>();
        request.put("data", agriculturalData);
        request.put("predictionType", "crop_yield");

        return webClientBuilder.build()
                .post()
                .uri(llmServiceUrl + "/api/llm/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Crop yield prediction received"))
                .doOnError(error -> log.error("Error requesting crop yield prediction: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Failed to get crop yield prediction", error);
                    return Mono.just(new HashMap<>());
                });
    }

    /**
     * Request food security analysis
     */
    public Mono<Map<String, Object>> analyzeFoodSecurity(Map<String, Object> data) {
        log.info("Requesting food security analysis");

        Map<String, Object> request = new HashMap<>();
        request.put("data", data);
        request.put("predictionType", "food_security_analysis");

        return webClientBuilder.build()
                .post()
                .uri(llmServiceUrl + "/api/llm/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Food security analysis received"))
                .doOnError(error -> log.error("Error requesting food security analysis: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Failed to get food security analysis", error);
                    return Mono.just(new HashMap<>());
                });
    }

    /**
     * Request anomaly detection in agricultural data
     */
    public Mono<Map<String, Object>> detectAnomalies(Long visualizationId, Map<String, Object> data) {
        log.info("Requesting anomaly detection for visualization: {}", visualizationId);

        Map<String, Object> request = new HashMap<>();
        request.put("visualizationId", visualizationId);
        request.put("data", data);
        request.put("analysisType", "anomaly_detection");

        return webClientBuilder.build()
                .post()
                .uri(llmServiceUrl + "/api/llm/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Anomaly detection results received"))
                .doOnError(error -> log.error("Error in anomaly detection: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.error("Failed anomaly detection", error);
                    return Mono.just(new HashMap<>());
                });
    }
}
