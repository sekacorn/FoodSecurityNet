package com.foodsecuritynet.agrivisualizer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for resource monitoring metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetrics {

    private Double cpuUsagePercent;
    private Long memoryUsedMb;
    private Long memoryTotalMb;
    private Double memoryUsagePercent;
    private Long gpuMemoryUsedMb;
    private Long gpuMemoryTotalMb;
    private Double gpuUsagePercent;
    private Integer activeThreads;
    private Integer threadPoolSize;
    private Long diskUsedMb;
    private Long diskTotalMb;
    private Double diskUsagePercent;
    private LocalDateTime timestamp;
    private String status;

    /**
     * Check if resources are at critical levels
     */
    public boolean isCritical() {
        return (cpuUsagePercent != null && cpuUsagePercent > 90) ||
               (memoryUsagePercent != null && memoryUsagePercent > 90) ||
               (gpuUsagePercent != null && gpuUsagePercent > 90);
    }

    /**
     * Check if resources are at warning levels
     */
    public boolean isWarning() {
        return (cpuUsagePercent != null && cpuUsagePercent > 75) ||
               (memoryUsagePercent != null && memoryUsagePercent > 75) ||
               (gpuUsagePercent != null && gpuUsagePercent > 75);
    }

    /**
     * Get resource status
     */
    public String getResourceStatus() {
        if (isCritical()) {
            return "CRITICAL";
        } else if (isWarning()) {
            return "WARNING";
        } else {
            return "NORMAL";
        }
    }
}
