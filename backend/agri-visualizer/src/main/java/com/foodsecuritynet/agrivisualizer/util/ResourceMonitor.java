package com.foodsecuritynet.agrivisualizer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ResourceMonitor {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public Map<String, Object> getResourceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // CPU metrics (approximation)
        double cpuUsage = getCpuUsage();
        metrics.put("cpuUsage", cpuUsage);

        // Memory metrics
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsage = (double) usedMemory / maxMemory * 100;

        metrics.put("memoryUsage", memoryUsage);
        metrics.put("usedMemoryMB", usedMemory / (1024 * 1024));
        metrics.put("maxMemoryMB", maxMemory / (1024 * 1024));

        // Thread metrics
        metrics.put("threadCount", threadBean.getThreadCount());
        metrics.put("peakThreadCount", threadBean.getPeakThreadCount());

        return metrics;
    }

    public Map<String, Object> getJvmMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
        metrics.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
        metrics.put("nonHeapMemoryUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
        metrics.put("nonHeapMemoryMax", memoryBean.getNonHeapMemoryUsage().getMax());

        return metrics;
    }

    public Map<String, Object> getThreadMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("threadCount", threadBean.getThreadCount());
        metrics.put("peakThreadCount", threadBean.getPeakThreadCount());
        metrics.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
        metrics.put("daemonThreadCount", threadBean.getDaemonThreadCount());

        return metrics;
    }

    private double getCpuUsage() {
        // Simple approximation - in production, use OperatingSystemMXBean
        Runtime runtime = Runtime.getRuntime();
        int availableProcessors = runtime.availableProcessors();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        return ((double) (totalMemory - freeMemory) / totalMemory) * 100;
    }
}
