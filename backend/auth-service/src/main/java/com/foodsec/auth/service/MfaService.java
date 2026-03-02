package com.foodsec.auth.service;

import com.foodsec.auth.dto.MfaSetupResponse;
import com.foodsec.auth.exception.AuthException;
import com.foodsec.auth.model.MfaBackupCode;
import com.foodsec.auth.model.User;
import com.foodsec.auth.repository.MfaBackupCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Service for Multi-Factor Authentication (MFA) operations.
 *
 * Implements TOTP (Time-based One-Time Password) authentication
 * and backup code generation for account recovery.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MfaService {

    private final MfaBackupCodeRepository backupCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${mfa.issuer:FoodSecurityNet}")
    private String issuer;

    @Value("${mfa.backup-code-expiration-days:30}")
    private int backupCodeExpirationDays;

    private static final int BACKUP_CODE_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;
    private static final String BACKUP_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    /**
     * Sets up MFA for a user
     *
     * @param user the user to set up MFA for
     * @return MFA setup response with secret and QR code
     */
    @Transactional
    public MfaSetupResponse setupMfa(User user) {
        log.info("Setting up MFA for user: {}", user.getUsername());

        // Generate secret key
        String secret = secretGenerator.generate();

        // Generate QR code data URI
        String qrCodeDataUri = generateQrCodeDataUri(secret, user.getEmail());

        // Generate provisioning URI
        String provisioningUri = generateProvisioningUri(secret, user.getEmail());

        // Generate backup codes
        List<String> backupCodes = generateBackupCodes(user);

        return MfaSetupResponse.builder()
            .secret(secret)
            .qrCodeDataUri(qrCodeDataUri)
            .provisioningUri(provisioningUri)
            .backupCodes(backupCodes)
            .accountName(user.getEmail())
            .issuer(issuer)
            .build();
    }

    /**
     * Verifies a TOTP code against a secret
     *
     * @param secret the TOTP secret
     * @param code the code to verify
     * @return true if code is valid
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        try {
            return verifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.warn("Error verifying TOTP code: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies a backup code
     *
     * @param user the user attempting authentication
     * @param code the backup code to verify
     * @return true if code is valid and marked as used
     */
    @Transactional
    public boolean verifyBackupCode(User user, String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        // Get all valid backup codes for the user
        List<MfaBackupCode> validCodes = backupCodeRepository.findValidCodesByUser(user, LocalDateTime.now());

        // Use constant-time comparison to prevent timing attacks
        for (MfaBackupCode backupCode : validCodes) {
            if (passwordEncoder.matches(code, backupCode.getCodeHash())) {
                backupCode.markAsUsed();
                backupCodeRepository.save(backupCode);
                log.info("Backup code used for user: {}", user.getUsername());
                return true;
            }
        }

        return false;
    }

    /**
     * Generates new backup codes for a user
     *
     * @param user the user to generate codes for
     * @return list of plaintext backup codes
     */
    @Transactional
    public List<String> generateBackupCodes(User user) {
        log.info("Generating backup codes for user: {}", user.getUsername());

        // Delete existing backup codes
        backupCodeRepository.deleteByUser(user);

        List<String> plaintextCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            // Generate random backup code
            StringBuilder codeBuilder = new StringBuilder(BACKUP_CODE_LENGTH);
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                int index = random.nextInt(BACKUP_CODE_CHARS.length());
                codeBuilder.append(BACKUP_CODE_CHARS.charAt(index));
            }
            String code = codeBuilder.toString();
            plaintextCodes.add(code);

            // Hash and store the code
            MfaBackupCode backupCode = MfaBackupCode.builder()
                .user(user)
                .codeHash(passwordEncoder.encode(code))
                .used(false)
                .expiresAt(LocalDateTime.now().plusDays(backupCodeExpirationDays))
                .build();

            backupCodeRepository.save(backupCode);
        }

        return plaintextCodes;
    }

    /**
     * Counts remaining valid backup codes for a user
     *
     * @param user the user to count codes for
     * @return number of valid backup codes
     */
    public long countRemainingBackupCodes(User user) {
        return backupCodeRepository.countValidCodesByUser(user, LocalDateTime.now());
    }

    /**
     * Generates a QR code data URI for TOTP setup
     *
     * @param secret the TOTP secret
     * @param accountName the account name (usually email)
     * @return data URI for the QR code image
     */
    private String generateQrCodeDataUri(String secret, String accountName) {
        try {
            String provisioningUri = generateProvisioningUri(secret, accountName);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(provisioningUri, BarcodeFormat.QR_CODE, 300, 300);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/png;base64," + base64Image;
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code", e);
            throw new AuthException("Failed to generate QR code", "QR_GENERATION_ERROR");
        }
    }

    /**
     * Generates a provisioning URI for TOTP setup
     *
     * @param secret the TOTP secret
     * @param accountName the account name (usually email)
     * @return provisioning URI
     */
    private String generateProvisioningUri(String secret, String accountName) {
        QrData data = new QrData.Builder()
            .label(accountName)
            .secret(secret)
            .issuer(issuer)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();

        return data.getUri();
    }

    /**
     * Cleans up expired backup codes
     *
     * @return number of codes deleted
     */
    @Transactional
    public int cleanupExpiredBackupCodes() {
        log.info("Cleaning up expired backup codes");
        return backupCodeRepository.deleteExpiredCodes(LocalDateTime.now());
    }
}
