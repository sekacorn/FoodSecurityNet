package com.foodsecuritynet.agrivisualizer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for creating visualizations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Visualization type is required")
    private String visualizationType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Data source is required")
    private Map<String, Object> dataSource;

    private Map<String, Object> visualizationConfig;

    private Boolean enable3D;

    private Boolean enableAiPredictions;

    private String colorScheme;

    private Integer width;

    private Integer height;

    private String projection;

    private Map<String, Object> filters;

    private String timeRange;

    private String aggregationType;

    private Boolean includeExport;

    private String[] exportFormats;
}
