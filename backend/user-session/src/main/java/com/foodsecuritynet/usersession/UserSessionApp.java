package com.foodsecuritynet.usersession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserSessionApp {
    public static void main(String[] args) {
        SpringApplication.run(UserSessionApp.class, args);
    }
}
