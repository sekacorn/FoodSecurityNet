package com.foodsec.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Gateway Route Configuration
 *
 * Configures routes to all microservices with:
 * - Path-based routing
 * - Circuit breaker patterns
 * - Request/response logging
 * - Load balancing
 */
@Configuration
public class GatewayConfig {

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    @Value("${services.agri-integrator.url}")
    private String agriIntegratorUrl;

    @Value("${services.agri-visualizer.url}")
    private String agriVisualizerUrl;

    @Value("${services.user-session.url}")
    private String userSessionUrl;

    @Value("${services.llm-service.url}")
    private String llmServiceUrl;

    @Value("${services.collaboration-service.url}")
    private String collaborationServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST)))
                        .uri(authServiceUrl))

                // Agricultural Data Integrator Routes
                .route("agri-integrator", r -> r
                        .path("/api/agri-data/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("agriIntegratorCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/agri-data"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)))
                        .uri(agriIntegratorUrl))

                // Agricultural Data Visualizer Routes
                .route("agri-visualizer", r -> r
                        .path("/api/visualizations/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("agriVisualizerCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/visualizations"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)))
                        .uri(agriVisualizerUrl))

                // User Session Service Routes
                .route("user-session", r -> r
                        .path("/api/sessions/**", "/api/preferences/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("userSessionCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/sessions"))
                                .retry(config -> config
                                        .setRetries(2)))
                        .uri(userSessionUrl))

                // LLM Service Routes
                .route("llm-service", r -> r
                        .path("/api/llm/**", "/api/insights/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("llmServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/llm"))
                                .retry(config -> config
                                        .setRetries(1))) // Fewer retries for LLM due to cost
                        .uri(llmServiceUrl))

                // Collaboration Service Routes
                .route("collaboration-service", r -> r
                        .path("/api/collaboration/**", "/api/forums/**", "/api/resources/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("collaborationCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/collaboration"))
                                .retry(config -> config
                                        .setRetries(2)))
                        .uri(collaborationServiceUrl))

                // Health Check Routes (No Authentication Required)
                .route("health-check", r -> r
                        .path("/actuator/health/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("no://op"))

                .build();
    }
}
