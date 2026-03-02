package com.foodsecuritynet.agrivisualizer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for visualization operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationResponse {

    private Long visualizationId;
    private Long userId;
    private String visualizationType;
    private String title;
    private String description;
    private String status;
    private Map<String, Object> visualizationData;
    private Map<String, Object> metadata;
    private String thumbnailUrl;
    private String fullImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ResourceMetrics resourceMetrics;
    private Map<String, Object> aiPredictions;
    private Integer renderTimeMs;
    private String exportUrl;
    private String shareUrl;

    /**
     * Create success response
     */
    public static VisualizationResponse success(Long visualizationId, String title, String visualizationType) {
        return VisualizationResponse.builder()
                .visualizationId(visualizationId)
                .title(title)
                .visualizationType(visualizationType)
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create processing response
     */
    public static VisualizationResponse processing(Long visualizationId, String title) {
        return VisualizationResponse.builder()
                .visualizationId(visualizationId)
                .title(title)
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response
     */
    public static VisualizationResponse error(String message) {
        return VisualizationResponse.builder()
                .status("ERROR")
                .description(message)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
