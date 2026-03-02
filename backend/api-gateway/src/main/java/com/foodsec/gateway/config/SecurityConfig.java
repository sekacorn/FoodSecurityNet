package com.foodsec.gateway.config;

import com.foodsec.gateway.filter.JwtAuthenticationFilter;
import com.foodsec.gateway.filter.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Security Configuration
 *
 * Configures Spring Security for the API Gateway with:
 * - JWT authentication
 * - Rate limiting
 * - Public endpoint access
 * - Stateless session management
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for stateless API
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Disable default form login and basic auth
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // Stateless session management
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // Configure authorization rules
                .authorizeExchange(exchange -> exchange
                        // Public endpoints - no authentication required
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()

                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )

                // Add custom filters
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(rateLimitingFilter, SecurityWebFiltersOrder.AUTHORIZATION)

                .build();
    }
}
