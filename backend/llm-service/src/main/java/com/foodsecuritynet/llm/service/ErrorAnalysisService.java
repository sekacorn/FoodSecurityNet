package com.foodsecuritynet.llm.service;

import com.foodsecuritynet.llm.model.ErrorLog;
import com.foodsecuritynet.llm.model.QueryContext;
import com.foodsecuritynet.llm.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorAnalysisService {

    private final LlmIntegrationService llmIntegrationService;
    private final ErrorLogRepository errorLogRepository;

    @Transactional
    public Mono<Map<String, Object>> analyzeError(String errorMessage, String errorType, Map<String, Object> context) {
        log.info("Analyzing error: type={}", errorType);

        // Save error log
        ErrorLog errorLog = ErrorLog.builder()
                .errorMessage(errorMessage)
                .errorType(errorType)
                .context(context)
                .userId(context.getOrDefault("userId", "system").toString())
                .build();

        errorLogRepository.save(errorLog);

        String prompt = String.format(
                "Analyze this agricultural system error and provide troubleshooting guidance:\n\n" +
                "Error Type: %s\n" +
                "Error Message: %s\n" +
                "Context: %s\n\n" +
                "Please provide:\n" +
                "1. Root cause analysis\n" +
                "2. Immediate steps to resolve\n" +
                "3. Prevention recommendations",
                errorType,
                errorMessage,
                context.toString()
        );

        QueryContext queryContext = QueryContext.builder()
                .query(prompt)
                .userId(context.getOrDefault("userId", "system").toString())
                .context(context)
                .build();

        return llmIntegrationService.processQuery(queryContext)
                .map(response -> {
                    Map<String, Object> analysis = new HashMap<>();
                    analysis.put("errorId", errorLog.getId());
                    analysis.put("errorType", errorType);
                    analysis.put("analysis", response.getResponse());
                    analysis.put("timestamp", errorLog.getCreatedAt());
                    return analysis;
                });
    }

    public Mono<Map<String, Object>> suggestFix(String issue, Map<String, Object> context) {
        log.info("Suggesting fix for issue");

        String prompt = String.format(
                "Suggest practical solutions for this agricultural issue:\n\n" +
                "Issue: %s\n" +
                "Context: %s\n\n" +
                "Provide step-by-step recommendations with rationale.",
                issue,
                context.toString()
        );

        QueryContext queryContext = QueryContext.builder()
                .query(prompt)
                .userId(context.getOrDefault("userId", "system").toString())
                .context(context)
                .build();

        return llmIntegrationService.processQuery(queryContext)
                .map(response -> {
                    Map<String, Object> suggestions = new HashMap<>();
                    suggestions.put("issue", issue);
                    suggestions.put("recommendations", response.getResponse());
                    suggestions.put("timestamp", response.getCreatedAt());
                    return suggestions;
                });
    }

    public List<ErrorLog> getErrorLogs(String userId) {
        return errorLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ErrorLog> getErrorLogsByType(String errorType) {
        return errorLogRepository.findByErrorTypeOrderByCreatedAtDesc(errorType);
    }
}
