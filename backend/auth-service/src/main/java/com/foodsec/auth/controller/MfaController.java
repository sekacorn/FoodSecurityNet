package com.foodsec.auth.controller;

import com.foodsec.auth.dto.MfaSetupResponse;
import com.foodsec.auth.dto.MfaVerifyRequest;
import com.foodsec.auth.exception.AuthException;
import com.foodsec.auth.model.User;
import com.foodsec.auth.repository.UserRepository;
import com.foodsec.auth.service.EmailService;
import com.foodsec.auth.service.JwtService;
import com.foodsec.auth.service.MfaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Multi-Factor Authentication (MFA) endpoints.
 *
 * Provides endpoints for:
 * - MFA setup and configuration
 * - MFA verification
 * - Backup code generation
 * - MFA status management
 */
@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
@Slf4j
public class MfaController {

    private final MfaService mfaService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Initiates MFA setup for the current user
     *
     * @param authorizationHeader Authorization header with JWT token
     * @return MFA setup information including QR code
     */
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        UUID userId = jwtService.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        if (user.getMfaEnabled()) {
            throw new AuthException("MFA is already enabled for this account", "MFA_ALREADY_ENABLED");
        }

        log.info("MFA setup initiated for user: {}", user.getUsername());

        MfaSetupResponse response = mfaService.setupMfa(user);

        return ResponseEntity.ok(ApiResponse.success(response, "MFA setup initiated. Please verify to enable."));
    }

    /**
     * Verifies and enables MFA for the current user
     *
     * @param request verification request with TOTP code
     * @param authorizationHeader Authorization header with JWT token
     * @return success response
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<MfaVerificationResponse>> verifyAndEnableMfa(
        @Valid @RequestBody MfaVerifyRequest request,
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        UUID userId = jwtService.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        log.info("MFA verification attempt for user: {}", user.getUsername());

        // Verify the code with the provided secret
        boolean isValid = mfaService.verifyCode(request.getSecret(), request.getCode());

        if (!isValid) {
            throw AuthException.mfaVerificationFailed();
        }

        // Enable MFA for the user
        user.setMfaEnabled(true);
        user.setMfaSecret(request.getSecret());
        userRepository.save(user);

        log.info("MFA enabled successfully for user: {}", user.getUsername());

        // Send notification email
        emailService.sendMfaEnabledNotification(user.getEmail(), user.getUsername());

        MfaVerificationResponse response = MfaVerificationResponse.builder()
            .mfaEnabled(true)
            .message("Two-factor authentication has been enabled successfully")
            .build();

        return ResponseEntity.ok(ApiResponse.success(response, "MFA enabled successfully"));
    }

    /**
     * Disables MFA for the current user
     *
     * @param request verification request with current TOTP code
     * @param authorizationHeader Authorization header with JWT token
     * @return success response
     */
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disableMfa(
        @Valid @RequestBody MfaVerifyRequest request,
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        UUID userId = jwtService.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        if (!user.getMfaEnabled()) {
            throw new AuthException("MFA is not enabled for this account", "MFA_NOT_ENABLED");
        }

        // Verify the current MFA code before disabling
        boolean isValid = mfaService.verifyCode(user.getMfaSecret(), request.getCode());

        if (!isValid) {
            throw AuthException.mfaVerificationFailed();
        }

        log.info("Disabling MFA for user: {}", user.getUsername());

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);

        // Send security alert
        emailService.sendSecurityAlert(
            user.getEmail(),
            user.getUsername(),
            "Two-factor authentication has been disabled on your account."
        );

        return ResponseEntity.ok(ApiResponse.success(null, "MFA disabled successfully"));
    }

    /**
     * Generates new backup codes for the current user
     *
     * @param authorizationHeader Authorization header with JWT token
     * @return list of backup codes
     */
    @PostMapping("/backup-codes")
    public ResponseEntity<ApiResponse<BackupCodesResponse>> generateBackupCodes(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        UUID userId = jwtService.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        if (!user.getMfaEnabled()) {
            throw new AuthException("MFA must be enabled to generate backup codes", "MFA_NOT_ENABLED");
        }

        log.info("Generating backup codes for user: {}", user.getUsername());

        List<String> backupCodes = mfaService.generateBackupCodes(user);

        BackupCodesResponse response = BackupCodesResponse.builder()
            .backupCodes(backupCodes)
            .message("Store these codes in a secure location. Each code can only be used once.")
            .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Backup codes generated successfully"));
    }

    /**
     * Gets the MFA status for the current user
     *
     * @param authorizationHeader Authorization header with JWT token
     * @return MFA status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<MfaStatusResponse>> getMfaStatus(
        @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = jwtService.extractTokenFromHeader(authorizationHeader);
        UUID userId = jwtService.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        long remainingBackupCodes = user.getMfaEnabled()
            ? mfaService.countRemainingBackupCodes(user)
            : 0;

        MfaStatusResponse response = MfaStatusResponse.builder()
            .mfaEnabled(user.getMfaEnabled())
            .remainingBackupCodes(remainingBackupCodes)
            .build();

        return ResponseEntity.ok(ApiResponse.success(response, "MFA status retrieved"));
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
     * MFA verification response
     */
    @lombok.Data
    @lombok.Builder
    public static class MfaVerificationResponse {
        private boolean mfaEnabled;
        private String message;
    }

    /**
     * Backup codes response
     */
    @lombok.Data
    @lombok.Builder
    public static class BackupCodesResponse {
        private List<String> backupCodes;
        private String message;
    }

    /**
     * MFA status response
     */
    @lombok.Data
    @lombok.Builder
    public static class MfaStatusResponse {
        private boolean mfaEnabled;
        private long remainingBackupCodes;
    }
}
