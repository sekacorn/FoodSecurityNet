package com.foodsecuritynet.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for LLM queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {

    private Long queryId;
    private String response;
    private String status;
    private String modelUsed;
    private Integer responseTimeMs;
    private Integer tokensUsed;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    private List<String> suggestions;
    private String errorMessage;

    /**
     * Create success response
     */
    public static QueryResponse success(Long queryId, String response, String modelUsed, Integer responseTimeMs) {
        return QueryResponse.builder()
                .queryId(queryId)
                .response(response)
                .status("SUCCESS")
                .modelUsed(modelUsed)
                .responseTimeMs(responseTimeMs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response
     */
    public static QueryResponse error(String errorMessage) {
        return QueryResponse.builder()
                .status("ERROR")
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create processing response
     */
    public static QueryResponse processing(Long queryId) {
        return QueryResponse.builder()
                .queryId(queryId)
                .status("PROCESSING")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
