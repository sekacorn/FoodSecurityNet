package com.foodsecuritynet.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for LLM queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Query is required")
    private String query;

    private String queryType;

    private Map<String, Object> context;

    private String visualizationId;

    private String sessionId;

    private Boolean includeHistory;

    private Integer maxTokens;

    private Double temperature;

    private String language;

    private String preferredModel;
}
