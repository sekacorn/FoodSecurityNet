package com.foodsec.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * Main Application class for FoodSecurityNet API Gateway
 *
 * This gateway serves as the single entry point for all client requests,
 * routing them to appropriate microservices with security, rate limiting,
 * and circuit breaker patterns.
 */
@SpringBootApplication
public class ApiGatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApp.class, args);
    }
}
