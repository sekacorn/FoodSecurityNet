package com.foodsecuritynet.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an LLM query
 */
@Entity
@Table(name = "llm_queries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 5000)
    private String query;

    @Column(length = 10000)
    private String response;

    @Column(name = "query_type")
    private String queryType;

    @Column(name = "context_data", columnDefinition = "TEXT")
    private String contextData;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
