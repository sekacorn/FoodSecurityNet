package com.foodsec.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for MFA setup response.
 *
 * Contains the secret key and QR code data needed to set up
 * TOTP-based two-factor authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaSetupResponse {

    /**
     * Base32-encoded secret key for TOTP generation
     */
    private String secret;

    /**
     * QR code data URI (data:image/png;base64,...)
     * Can be displayed as an image for easy scanning
     */
    private String qrCodeDataUri;

    /**
     * Provisioning URI (otpauth://totp/...)
     * Can be used to manually add the account to authenticator apps
     */
    private String provisioningUri;

    /**
     * List of backup codes for account recovery
     * These should be saved securely by the user
     */
    private List<String> backupCodes;

    /**
     * Account name displayed in authenticator app
     */
    private String accountName;

    /**
     * Issuer name displayed in authenticator app
     */
    private String issuer;
}
