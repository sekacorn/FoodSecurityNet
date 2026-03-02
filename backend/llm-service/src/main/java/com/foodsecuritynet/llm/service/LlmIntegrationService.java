package com.foodsecuritynet.llm.service;

import com.foodsecuritynet.llm.model.LlmResponse;
import com.foodsecuritynet.llm.model.QueryContext;
import com.foodsecuritynet.llm.repository.LlmResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmIntegrationService {

    private final WebClient.Builder webClientBuilder;
    private final LlmResponseRepository llmResponseRepository;

    @Value("${app.llm.api-url:https://api.openai.com/v1/chat/completions}")
    private String llmApiUrl;

    @Value("${app.llm.api-key:}")
    private String llmApiKey;

    @Value("${app.llm.model:gpt-3.5-turbo}")
    private String llmModel;

    @Transactional
    public Mono<LlmResponse> processQuery(QueryContext queryContext) {
        log.info("Processing query for user: {}", queryContext.getUserId());

        return callLlmApi(queryContext)
                .flatMap(responseText -> {
                    LlmResponse response = LlmResponse.builder()
                            .userId(queryContext.getUserId())
                            .query(queryContext.getQuery())
                            .response(responseText)
                            .model(llmModel)
                            .context(queryContext.getContext())
                            .build();

                    LlmResponse savedResponse = llmResponseRepository.save(response);
                    return Mono.just(savedResponse);
                })
                .onErrorResume(error -> {
                    log.error("Error processing query", error);
                    return Mono.error(new RuntimeException("Failed to process query: " + error.getMessage()));
                });
    }

    private Mono<String> callLlmApi(QueryContext queryContext) {
        // Prepare request body for LLM API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", llmModel);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful agricultural expert assistant for FoodSecurityNet."),
                Map.of("role", "user", "content", queryContext.getQuery())
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        // If API key is not configured, return mock response
        if (llmApiKey == null || llmApiKey.isEmpty()) {
            log.warn("LLM API key not configured, returning mock response");
            return Mono.just(generateMockResponse(queryContext.getQuery()));
        }

        WebClient webClient = webClientBuilder
                .baseUrl(llmApiUrl)
                .defaultHeader("Authorization", "Bearer " + llmApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        return message.get("content").toString();
                    }
                    return "No response from LLM";
                })
                .onErrorResume(error -> {
                    log.error("Error calling LLM API, falling back to mock response", error);
                    return Mono.just(generateMockResponse(queryContext.getQuery()));
                });
    }

    public Mono<Map<String, Object>> analyzeData(String analysisType, Map<String, Object> data, String userId) {
        log.info("Analyzing data: type={}, user={}", analysisType, userId);

        String prompt = String.format(
                "Analyze the following %s data and provide insights: %s",
                analysisType,
                data.toString()
        );

        QueryContext context = QueryContext.builder()
                .query(prompt)
                .userId(userId)
                .context(Map.of("analysisType", analysisType, "data", data))
                .build();

        return processQuery(context)
                .map(response -> Map.of(
                        "analysisType", analysisType,
                        "insights", response.getResponse(),
                        "timestamp", LocalDateTime.now()
                ));
    }

    public List<LlmResponse> getQueryHistory(String userId) {
        return llmResponseRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private String generateMockResponse(String query) {
        return String.format(
                "This is a mock response for your query: '%s'. " +
                "To get real AI-powered responses, please configure the LLM API key in application.yml. " +
                "The FoodSecurityNet system can help you with agricultural data analysis, visualization, " +
                "and insights based on your data.",
                query
        );
    }
}
