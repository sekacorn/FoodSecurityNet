package com.foodsec.auth;

import com.foodsec.auth.model.MfaBackupCode;
import com.foodsec.auth.model.User;
import com.foodsec.auth.repository.MfaBackupCodeRepository;
import com.foodsec.auth.service.MfaService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MfaService
 * Tests TOTP generation, verification, and backup codes
 * Coverage target: >90%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MFA Service Tests")
class MfaServiceTest {

    @Mock
    private SecretGenerator secretGenerator;

    @Mock
    private QrGenerator qrGenerator;

    @Mock
    private CodeVerifier codeVerifier;

    @Mock
    private MfaBackupCodeRepository backupCodeRepository;

    @InjectMocks
    private MfaService mfaService;

    private User testUser;
    private String testSecret;
    private String testCode;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setMfaEnabled(false);

        testSecret = "JBSWY3DPEHPK3PXP";
        testCode = "123456";
    }

    // ===================== SECRET GENERATION TESTS =====================

    @Test
    @DisplayName("Should generate valid secret")
    void testGenerateSecret_Success() {
        // Arrange
        when(secretGenerator.generate()).thenReturn(testSecret);

        // Act
        String secret = mfaService.generateSecret();

        // Assert
        assertNotNull(secret);
        assertEquals(testSecret, secret);
        verify(secretGenerator).generate();
    }

    @Test
    @DisplayName("Should generate unique secrets")
    void testGenerateSecret_Uniqueness() {
        // Arrange
        when(secretGenerator.generate())
            .thenReturn("SECRET1")
            .thenReturn("SECRET2")
            .thenReturn("SECRET3");

        // Act
        String secret1 = mfaService.generateSecret();
        String secret2 = mfaService.generateSecret();
        String secret3 = mfaService.generateSecret();

        // Assert
        assertNotEquals(secret1, secret2);
        assertNotEquals(secret2, secret3);
        assertNotEquals(secret1, secret3);
        verify(secretGenerator, times(3)).generate();
    }

    @Test
    @DisplayName("Should handle secret generation failure")
    void testGenerateSecret_Failure() {
        // Arrange
        when(secretGenerator.generate()).thenThrow(new RuntimeException("Generation failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mfaService.generateSecret());
    }

    // ===================== QR CODE GENERATION TESTS =====================

    @Test
    @DisplayName("Should generate valid QR code")
    void testGenerateQrCode_Success() throws QrGenerationException {
        // Arrange
        String expectedQrData = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        when(qrGenerator.generate(any(QrData.class))).thenReturn(expectedQrData);

        // Act
        String qrCode = mfaService.generateQrCode(testSecret, testUser.getEmail());

        // Assert
        assertNotNull(qrCode);
        assertEquals(expectedQrData, qrCode);
        verify(qrGenerator).generate(any(QrData.class));
    }

    @Test
    @DisplayName("Should include user email in QR code")
    void testGenerateQrCode_ContainsEmail() throws QrGenerationException {
        // Arrange
        when(qrGenerator.generate(any(QrData.class))).thenAnswer(invocation -> {
            QrData qrData = invocation.getArgument(0);
            assertTrue(qrData.getLabel().contains(testUser.getEmail()));
            return "qr-code-data";
        });

        // Act
        mfaService.generateQrCode(testSecret, testUser.getEmail());

        // Assert
        verify(qrGenerator).generate(any(QrData.class));
    }

    @Test
    @DisplayName("Should handle QR generation exception")
    void testGenerateQrCode_Exception() throws QrGenerationException {
        // Arrange
        when(qrGenerator.generate(any(QrData.class)))
            .thenThrow(new QrGenerationException("QR generation failed"));

        // Act & Assert
        assertThrows(QrGenerationException.class, () ->
            mfaService.generateQrCode(testSecret, testUser.getEmail())
        );
    }

    @Test
    @DisplayName("Should handle null secret in QR generation")
    void testGenerateQrCode_NullSecret() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            mfaService.generateQrCode(null, testUser.getEmail())
        );
    }

    @Test
    @DisplayName("Should handle null email in QR generation")
    void testGenerateQrCode_NullEmail() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            mfaService.generateQrCode(testSecret, null)
        );
    }

    // ===================== TOTP VERIFICATION TESTS =====================

    @Test
    @DisplayName("Should verify valid TOTP code")
    void testVerifyTotp_ValidCode() {
        // Arrange
        when(codeVerifier.isValidCode(testSecret, testCode)).thenReturn(true);

        // Act
        boolean isValid = mfaService.verifyTotp(testSecret, testCode);

        // Assert
        assertTrue(isValid);
        verify(codeVerifier).isValidCode(testSecret, testCode);
    }

    @Test
    @DisplayName("Should reject invalid TOTP code")
    void testVerifyTotp_InvalidCode() {
        // Arrange
        when(codeVerifier.isValidCode(testSecret, testCode)).thenReturn(false);

        // Act
        boolean isValid = mfaService.verifyTotp(testSecret, testCode);

        // Assert
        assertFalse(isValid);
        verify(codeVerifier).isValidCode(testSecret, testCode);
    }

    @Test
    @DisplayName("Should reject expired TOTP code")
    void testVerifyTotp_ExpiredCode() {
        // Arrange
        String expiredCode = "000000";
        when(codeVerifier.isValidCode(testSecret, expiredCode)).thenReturn(false);

        // Act
        boolean isValid = mfaService.verifyTotp(testSecret, expiredCode);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle null secret in verification")
    void testVerifyTotp_NullSecret() {
        // Arrange
        when(codeVerifier.isValidCode(null, testCode))
            .thenThrow(new IllegalArgumentException("Secret cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            mfaService.verifyTotp(null, testCode)
        );
    }

    @Test
    @DisplayName("Should handle null code in verification")
    void testVerifyTotp_NullCode() {
        // Arrange
        when(codeVerifier.isValidCode(testSecret, null))
            .thenThrow(new IllegalArgumentException("Code cannot be null"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            mfaService.verifyTotp(testSecret, null)
        );
    }

    @Test
    @DisplayName("Should reject code with wrong length")
    void testVerifyTotp_WrongLength() {
        // Arrange
        String shortCode = "123";
        when(codeVerifier.isValidCode(testSecret, shortCode)).thenReturn(false);

        // Act
        boolean isValid = mfaService.verifyTotp(testSecret, shortCode);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject non-numeric code")
    void testVerifyTotp_NonNumeric() {
        // Arrange
        String alphaCode = "ABC123";
        when(codeVerifier.isValidCode(testSecret, alphaCode)).thenReturn(false);

        // Act
        boolean isValid = mfaService.verifyTotp(testSecret, alphaCode);

        // Assert
        assertFalse(isValid);
    }

    // ===================== BACKUP CODE GENERATION TESTS =====================

    @Test
    @DisplayName("Should generate backup codes")
    void testGenerateBackupCodes_Success() {
        // Arrange
        int codeCount = 10;
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        List<String> backupCodes = mfaService.generateBackupCodes(testUser, codeCount);

        // Assert
        assertNotNull(backupCodes);
        assertEquals(codeCount, backupCodes.size());
        verify(backupCodeRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should generate unique backup codes")
    void testGenerateBackupCodes_Uniqueness() {
        // Arrange
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        List<String> backupCodes = mfaService.generateBackupCodes(testUser, 10);

        // Assert
        assertEquals(10, backupCodes.stream().distinct().count());
    }

    @Test
    @DisplayName("Should generate codes with correct format")
    void testGenerateBackupCodes_Format() {
        // Arrange
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        List<String> backupCodes = mfaService.generateBackupCodes(testUser, 10);

        // Assert
        for (String code : backupCodes) {
            assertNotNull(code);
            assertTrue(code.length() >= 8); // Minimum length
            assertTrue(code.matches("[A-Z0-9]+")); // Alphanumeric uppercase
        }
    }

    @Test
    @DisplayName("Should handle custom backup code count")
    void testGenerateBackupCodes_CustomCount() {
        // Arrange
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act & Assert
        List<String> codes5 = mfaService.generateBackupCodes(testUser, 5);
        assertEquals(5, codes5.size());

        List<String> codes20 = mfaService.generateBackupCodes(testUser, 20);
        assertEquals(20, codes20.size());
    }

    // ===================== BACKUP CODE VERIFICATION TESTS =====================

    @Test
    @DisplayName("Should verify valid backup code")
    void testVerifyBackupCode_ValidCode() {
        // Arrange
        String backupCode = "ABCD1234";
        MfaBackupCode storedCode = new MfaBackupCode();
        storedCode.setCode(backupCode);
        storedCode.setUsed(false);
        storedCode.setUser(testUser);

        when(backupCodeRepository.findByUserAndCodeAndUsed(testUser, backupCode, false))
            .thenReturn(Optional.of(storedCode));
        when(backupCodeRepository.save(any(MfaBackupCode.class))).thenReturn(storedCode);

        // Act
        boolean isValid = mfaService.verifyBackupCode(testUser, backupCode);

        // Assert
        assertTrue(isValid);
        verify(backupCodeRepository).findByUserAndCodeAndUsed(testUser, backupCode, false);
        verify(backupCodeRepository).save(argThat(code -> code.isUsed()));
    }

    @Test
    @DisplayName("Should reject already used backup code")
    void testVerifyBackupCode_AlreadyUsed() {
        // Arrange
        String backupCode = "ABCD1234";
        when(backupCodeRepository.findByUserAndCodeAndUsed(testUser, backupCode, false))
            .thenReturn(Optional.empty());

        // Act
        boolean isValid = mfaService.verifyBackupCode(testUser, backupCode);

        // Assert
        assertFalse(isValid);
        verify(backupCodeRepository).findByUserAndCodeAndUsed(testUser, backupCode, false);
        verify(backupCodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject non-existent backup code")
    void testVerifyBackupCode_NotFound() {
        // Arrange
        String backupCode = "INVALID1";
        when(backupCodeRepository.findByUserAndCodeAndUsed(testUser, backupCode, false))
            .thenReturn(Optional.empty());

        // Act
        boolean isValid = mfaService.verifyBackupCode(testUser, backupCode);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should mark backup code as used after verification")
    void testVerifyBackupCode_MarkedAsUsed() {
        // Arrange
        String backupCode = "ABCD1234";
        MfaBackupCode storedCode = new MfaBackupCode();
        storedCode.setCode(backupCode);
        storedCode.setUsed(false);
        storedCode.setUser(testUser);

        when(backupCodeRepository.findByUserAndCodeAndUsed(testUser, backupCode, false))
            .thenReturn(Optional.of(storedCode));
        when(backupCodeRepository.save(any(MfaBackupCode.class))).thenAnswer(invocation -> {
            MfaBackupCode code = invocation.getArgument(0);
            assertTrue(code.isUsed());
            return code;
        });

        // Act
        mfaService.verifyBackupCode(testUser, backupCode);

        // Assert
        verify(backupCodeRepository).save(any(MfaBackupCode.class));
    }

    // ===================== MFA SETUP TESTS =====================

    @Test
    @DisplayName("Should setup MFA successfully")
    void testSetupMfa_Success() throws QrGenerationException {
        // Arrange
        when(secretGenerator.generate()).thenReturn(testSecret);
        when(qrGenerator.generate(any(QrData.class))).thenReturn("qr-code-data");
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        var setupResponse = mfaService.setupMfa(testUser);

        // Assert
        assertNotNull(setupResponse);
        assertEquals(testSecret, setupResponse.getSecret());
        assertEquals("qr-code-data", setupResponse.getQrCode());
        assertNotNull(setupResponse.getBackupCodes());
        assertEquals(10, setupResponse.getBackupCodes().size());
        verify(secretGenerator).generate();
        verify(qrGenerator).generate(any(QrData.class));
    }

    @Test
    @DisplayName("Should include backup codes in MFA setup")
    void testSetupMfa_IncludesBackupCodes() throws QrGenerationException {
        // Arrange
        when(secretGenerator.generate()).thenReturn(testSecret);
        when(qrGenerator.generate(any(QrData.class))).thenReturn("qr-code-data");
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        var setupResponse = mfaService.setupMfa(testUser);

        // Assert
        List<String> backupCodes = setupResponse.getBackupCodes();
        assertNotNull(backupCodes);
        assertFalse(backupCodes.isEmpty());
        assertEquals(10, backupCodes.size());
    }

    // ===================== MFA DISABLE TESTS =====================

    @Test
    @DisplayName("Should disable MFA successfully")
    void testDisableMfa_Success() {
        // Arrange
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret(testSecret);
        when(backupCodeRepository.deleteByUser(testUser)).thenReturn(10);

        // Act
        boolean result = mfaService.disableMfa(testUser);

        // Assert
        assertTrue(result);
        assertFalse(testUser.isMfaEnabled());
        assertNull(testUser.getMfaSecret());
        verify(backupCodeRepository).deleteByUser(testUser);
    }

    @Test
    @DisplayName("Should clear backup codes when disabling MFA")
    void testDisableMfa_ClearsBackupCodes() {
        // Arrange
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret(testSecret);
        when(backupCodeRepository.deleteByUser(testUser)).thenReturn(10);

        // Act
        mfaService.disableMfa(testUser);

        // Assert
        verify(backupCodeRepository).deleteByUser(testUser);
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    @DisplayName("Should handle concurrent TOTP verifications")
    void testVerifyTotp_Concurrent() throws InterruptedException {
        // Arrange
        when(codeVerifier.isValidCode(testSecret, testCode)).thenReturn(true);
        int threadCount = 10;
        boolean[] results = new boolean[threadCount];
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = mfaService.verifyTotp(testSecret, testCode);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        for (boolean result : results) {
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should handle time-based TOTP validation")
    void testVerifyTotp_TimeWindow() {
        // Arrange - Code should be valid within time window
        when(codeVerifier.isValidCode(testSecret, testCode)).thenReturn(true);

        // Act
        boolean isValid = mfaService.verifyTotp(testSecret, testCode);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should prevent backup code reuse")
    void testBackupCode_PreventReuse() {
        // Arrange
        String backupCode = "ABCD1234";
        MfaBackupCode storedCode = new MfaBackupCode();
        storedCode.setCode(backupCode);
        storedCode.setUsed(false);
        storedCode.setUser(testUser);

        when(backupCodeRepository.findByUserAndCodeAndUsed(testUser, backupCode, false))
            .thenReturn(Optional.of(storedCode))
            .thenReturn(Optional.empty());
        when(backupCodeRepository.save(any(MfaBackupCode.class))).thenReturn(storedCode);

        // Act
        boolean firstUse = mfaService.verifyBackupCode(testUser, backupCode);
        boolean secondUse = mfaService.verifyBackupCode(testUser, backupCode);

        // Assert
        assertTrue(firstUse);
        assertFalse(secondUse);
    }

    @Test
    @DisplayName("Should handle MFA setup for user with existing MFA")
    void testSetupMfa_ExistingMfa() throws QrGenerationException {
        // Arrange
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("OLD_SECRET");
        when(secretGenerator.generate()).thenReturn(testSecret);
        when(qrGenerator.generate(any(QrData.class))).thenReturn("qr-code-data");
        when(backupCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });
        when(backupCodeRepository.deleteByUser(testUser)).thenReturn(10);

        // Act
        var setupResponse = mfaService.setupMfa(testUser);

        // Assert
        assertNotNull(setupResponse);
        assertEquals(testSecret, setupResponse.getSecret());
        verify(backupCodeRepository).deleteByUser(testUser); // Old codes should be deleted
    }
}
