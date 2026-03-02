package com.foodsec.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for the FoodSecurityNet Authentication Service.
 *
 * This service provides comprehensive authentication and authorization capabilities including:
 * - Traditional username/password authentication
 * - Multi-factor authentication (TOTP-based)
 * - Single Sign-On (SSO) integration with Google, Microsoft, Okta, and SAML
 * - JWT-based token management
 * - Role-based access control
 *
 * @author FoodSecurityNet Team
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AuthServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApp.class, args);
    }
}
