package com.foodsecuritynet.agrivisualizer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Slf4j
public class MultithreadingManager {

    @Bean(name = "visualizationExecutor")
    public Executor visualizationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("viz-");
        executor.setRejectedExecutionHandler((r, executor1) -> {
            log.warn("Task rejected, thread pool is full");
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "exportExecutor")
    public Executor exportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("export-");
        executor.initialize();
        return executor;
    }
}
