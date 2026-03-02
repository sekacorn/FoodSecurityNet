package com.foodsecuritynet.llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LLMApp {
    public static void main(String[] args) {
        SpringApplication.run(LLMApp.class, args);
    }
}
