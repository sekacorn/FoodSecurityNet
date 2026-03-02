package com.foodsecuritynet.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryContext {
    private String query;
    private String userId;
    private String mbtiType;
    private Map<String, Object> context;
}
