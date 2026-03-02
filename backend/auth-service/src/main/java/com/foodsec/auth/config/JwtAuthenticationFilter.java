package com.foodsec.auth.config;

import com.foodsec.auth.exception.UnauthorizedException;
import com.foodsec.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT authentication filter for processing and validating JWT tokens.
 *
 * This filter:
 * - Extracts JWT tokens from the Authorization header
 * - Validates tokens using JwtService
 * - Populates Spring Security context with authentication details
 * - Handles authentication errors gracefully
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip filter for public endpoints
        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token from Authorization header
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // Validate token and extract claims
                Claims claims = jwtService.validateToken(token);

                // Check if it's an access token (not a refresh token)
                String tokenType = claims.get("tokenType", String.class);
                if (!"ACCESS".equals(tokenType)) {
                    log.warn("Invalid token type used for authentication: {}", tokenType);
                    throw UnauthorizedException.invalidToken();
                }

                // Extract user information from claims
                String userId = claims.get("userId", String.class);
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);

                // Create authorities from role
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                    );

                // Set additional details
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Successfully authenticated user: {} with role: {}", username, role);
            }
        } catch (UnauthorizedException e) {
            log.warn("Authentication failed: {}", e.getMessage());
            // Clear security context on authentication failure
            SecurityContextHolder.clearContext();
            // Don't throw exception here, let the security filter chain handle it
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication", e);
            SecurityContextHolder.clearContext();
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the request Authorization header
     *
     * @param request the HTTP request
     * @return JWT token or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Checks if the request URI is a public endpoint that doesn't require authentication
     *
     * @param uri the request URI
     * @return true if endpoint is public
     */
    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/api/auth/register") ||
               uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/refresh") ||
               uri.startsWith("/api/auth/health") ||
               uri.startsWith("/api/auth/sso") ||
               uri.startsWith("/oauth2") ||
               uri.startsWith("/login/oauth2") ||
               uri.startsWith("/saml2") ||
               uri.equals("/error");
    }

    /**
     * Determines if the filter should be applied to the request
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // The filter will handle the check internally, so always apply it
        return false;
    }
}
