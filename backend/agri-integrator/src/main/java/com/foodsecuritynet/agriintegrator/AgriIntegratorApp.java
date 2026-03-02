package com.foodsecuritynet.agriintegrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgriIntegratorApp {
    public static void main(String[] args) {
        SpringApplication.run(AgriIntegratorApp.class, args);
    }
}
