package com.foodsec.auth.controller;

import com.foodsec.auth.dto.*;
import com.foodsec.auth.model.User;
import com.foodsec.auth.service.AuthService;
import com.foodsec.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * Provides endpoints for:
 * - User registration
 * - Login/logout
 * - Token refresh
 * - Current user information
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Registers a new user
     *
     * @param request registration request
     * @return created user information
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());

        User user = authService.register(request);
        UserDto userDto = convertToDto(user);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(userDto, "User registered successfully"));
    }

    /**
     * Authenticates a user
     *
     * @param request login request
     * @return login response with tokens or MFA requirement
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getUsernameOrEmail());

        LoginResponse response = authService.login(request);

        String message = response.isMfaRequired()
            ? "MFA verification required"
            : "Login successful";

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    /**
     * Refreshes access token
     *
     * @param request refresh token request
     * @return new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.debug("Token refresh request received");

        LoginResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Logs out the current user
     *
     * @param authorizationHeader Authorization header with JWT token
     * @return success response
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        if (token != null) {
            // Token is validated by the filter, just return success
            // In a production system, you might want to blacklist the token
            log.info("User logged out");
        }

        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    /**
     * Gets current user information
     *
     * @param authorizationHeader Authorization header with JWT token
     * @return current user information
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        UserDto user = authService.getCurrentUser(token);

        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatus>> health() {
        HealthStatus status = HealthStatus.builder()
            .status("UP")
            .service("auth-service")
            .build();

        return ResponseEntity.ok(ApiResponse.success(status, "Service is healthy"));
    }

    /**
     * Converts User entity to UserDto
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .mbtiType(user.getMbtiType())
            .ssoProvider(user.getSsoProvider())
            .mfaEnabled(user.getMfaEnabled())
            .emailVerified(user.getEmailVerified())
            .isActive(user.getIsActive())
            .lastLogin(user.getLastLogin())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    /**
     * Generic API response wrapper
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data, String message) {
            return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
        }
    }

    /**
     * Health status response
     */
    @lombok.Data
    @lombok.Builder
    public static class HealthStatus {
        private String status;
        private String service;
    }
}
