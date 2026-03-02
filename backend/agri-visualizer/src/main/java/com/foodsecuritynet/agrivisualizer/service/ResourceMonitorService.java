package com.foodsecuritynet.agrivisualizer.service;

import com.foodsecuritynet.agrivisualizer.util.ResourceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceMonitorService {

    private final ResourceMonitor resourceMonitor;

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void monitorResources() {
        Map<String, Object> metrics = resourceMonitor.getResourceMetrics();
        log.debug("Resource metrics: {}", metrics);

        // Check thresholds and log warnings
        double cpuUsage = (double) metrics.get("cpuUsage");
        double memoryUsage = (double) metrics.get("memoryUsage");

        if (cpuUsage > 80.0) {
            log.warn("High CPU usage detected: {}%", cpuUsage);
        }

        if (memoryUsage > 85.0) {
            log.warn("High memory usage detected: {}%", memoryUsage);
        }
    }

    public Map<String, Object> getCurrentMetrics() {
        return resourceMonitor.getResourceMetrics();
    }

    public Map<String, Object> getDetailedMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("system", resourceMonitor.getResourceMetrics());
        metrics.put("jvm", resourceMonitor.getJvmMetrics());
        metrics.put("threads", resourceMonitor.getThreadMetrics());
        return metrics;
    }
}
