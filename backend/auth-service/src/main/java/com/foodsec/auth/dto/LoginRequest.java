package com.foodsec.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login requests.
 *
 * Supports login with either username or email along with password.
 * Optionally includes MFA code or backup code for two-factor authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * TOTP code from authenticator app (6 digits)
     */
    private String mfaCode;

    /**
     * MFA backup code (alternative to TOTP code)
     */
    private String backupCode;

    /**
     * Device fingerprint for security tracking
     */
    private String deviceFingerprint;

    /**
     * Remember me flag for extended session duration
     */
    @Builder.Default
    private boolean rememberMe = false;
}
