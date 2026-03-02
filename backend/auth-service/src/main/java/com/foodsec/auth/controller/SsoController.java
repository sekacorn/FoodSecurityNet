package com.foodsec.auth.controller;

import com.foodsec.auth.dto.LoginResponse;
import com.foodsec.auth.model.SsoProvider;
import com.foodsec.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for Single Sign-On (SSO) authentication endpoints.
 *
 * Handles OAuth2 and SAML authentication callbacks from external providers:
 * - Google OAuth2
 * - Microsoft Azure AD
 * - Okta
 * - Generic SAML 2.0
 */
@RestController
@RequestMapping("/api/auth/sso")
@RequiredArgsConstructor
@Slf4j
public class SsoController {

    private final AuthService authService;

    /**
     * Handles OAuth2 login success callback
     *
     * @param authentication OAuth2 authentication token
     * @return login response with tokens
     */
    @GetMapping("/callback/{provider}")
    public ResponseEntity<ApiResponse<LoginResponse>> handleOAuth2Callback(
        @PathVariable String provider,
        OAuth2AuthenticationToken authentication
    ) {
        log.info("SSO callback received for provider: {}", provider);

        OAuth2User oauth2User = authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Extract user information based on provider
        String email = extractEmail(provider, attributes);
        String fullName = extractFullName(provider, attributes);
        String ssoSubject = extractSubject(provider, attributes);

        SsoProvider ssoProvider = SsoProvider.fromProviderId(provider);

        LoginResponse response = authService.loginWithSSO(ssoProvider, ssoSubject, email, fullName);

        return ResponseEntity.ok(ApiResponse.success(response, "SSO login successful"));
    }

    /**
     * Handles SAML login success callback
     *
     * @param attributes SAML assertion attributes
     * @return login response with tokens
     */
    @PostMapping("/saml/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> handleSamlCallback(
        @RequestBody Map<String, Object> attributes
    ) {
        log.info("SAML callback received");

        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("fullName");
        String ssoSubject = (String) attributes.get("nameId");

        LoginResponse response = authService.loginWithSSO(
            SsoProvider.SAML,
            ssoSubject,
            email,
            fullName
        );

        return ResponseEntity.ok(ApiResponse.success(response, "SAML login successful"));
    }

    /**
     * Initiates SSO login for a provider
     *
     * @param provider the SSO provider
     * @return redirect information
     */
    @GetMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<SsoLoginInfo>> initiateSsoLogin(@PathVariable String provider) {
        log.info("Initiating SSO login for provider: {}", provider);

        SsoProvider.fromProviderId(provider); // Validate provider

        SsoLoginInfo info = SsoLoginInfo.builder()
            .provider(provider)
            .authorizationUrl("/oauth2/authorization/" + provider)
            .message("Redirect to authorization URL to begin SSO flow")
            .build();

        return ResponseEntity.ok(ApiResponse.success(info, "SSO login initiated"));
    }

    /**
     * Extracts email from OAuth2 attributes based on provider
     */
    private String extractEmail(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "microsoft" -> (String) attributes.get("mail");
            case "okta" -> (String) attributes.get("email");
            default -> (String) attributes.get("email");
        };
    }

    /**
     * Extracts full name from OAuth2 attributes based on provider
     */
    private String extractFullName(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("name");
            case "microsoft" -> (String) attributes.get("displayName");
            case "okta" -> (String) attributes.get("name");
            default -> {
                String firstName = (String) attributes.get("given_name");
                String lastName = (String) attributes.get("family_name");
                yield (firstName != null && lastName != null)
                    ? firstName + " " + lastName
                    : (String) attributes.get("name");
            }
        };
    }

    /**
     * Extracts subject ID from OAuth2 attributes based on provider
     */
    private String extractSubject(String provider, Map<String, Object> attributes) {
        return switch (provider.toLowerCase()) {
            case "google" -> (String) attributes.get("sub");
            case "microsoft" -> (String) attributes.get("id");
            case "okta" -> (String) attributes.get("sub");
            default -> (String) attributes.get("sub");
        };
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
    }

    /**
     * SSO login information
     */
    @lombok.Data
    @lombok.Builder
    public static class SsoLoginInfo {
        private String provider;
        private String authorizationUrl;
        private String message;
    }
}
