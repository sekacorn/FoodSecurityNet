package com.foodsecuritynet.collaboration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CollaborationApp {
    public static void main(String[] args) {
        SpringApplication.run(CollaborationApp.class, args);
    }
}
