package com.foodsec.auth.dto;

import com.foodsec.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for login response.
 *
 * Contains authentication tokens and user information returned
 * after successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT access token for API authentication
     */
    private String accessToken;

    /**
     * Refresh token for obtaining new access tokens
     */
    private String refreshToken;

    /**
     * Token type (usually "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds
     */
    private Long expiresIn;

    /**
     * User ID
     */
    private UUID userId;

    /**
     * Username
     */
    private String username;

    /**
     * Email address
     */
    private String email;

    /**
     * User's full name
     */
    private String fullName;

    /**
     * User's role
     */
    private Role role;

    /**
     * MBTI personality type
     */
    private String mbtiType;

    /**
     * Whether MFA is enabled for this user
     */
    private boolean mfaEnabled;

    /**
     * Whether MFA verification is required for this login attempt
     */
    private boolean mfaRequired;

    /**
     * Whether email is verified
     */
    private boolean emailVerified;

    /**
     * Creates a response indicating MFA is required
     *
     * @param userId the user's ID
     * @param mfaEnabled whether MFA is enabled
     * @return LoginResponse with mfaRequired flag set
     */
    public static LoginResponse mfaRequired(UUID userId, boolean mfaEnabled) {
        return LoginResponse.builder()
            .userId(userId)
            .mfaEnabled(mfaEnabled)
            .mfaRequired(true)
            .build();
    }
}
