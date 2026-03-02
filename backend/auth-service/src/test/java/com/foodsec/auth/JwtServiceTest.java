package com.foodsec.auth;

import com.foodsec.auth.model.Role;
import com.foodsec.auth.model.User;
import com.foodsec.auth.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService
 * Tests JWT generation, validation, and extraction
 * Coverage target: >90%
 */
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyThatIsLongEnoughForHS256AlgorithmAndSecureForTesting",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=86400000"
})
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.RESEARCHER);
        testUser.setActive(true);
    }

    // ===================== TOKEN GENERATION TESTS =====================

    @Test
    @DisplayName("Should generate valid access token")
    void testGenerateAccessToken_Success() {
        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken_Success() {
        // Act
        String token = jwtService.generateRefreshToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Should generate different tokens for same user")
    void testGenerateToken_UniquenessPerCall() {
        // Act
        String token1 = jwtService.generateAccessToken(testUser);
        String token2 = jwtService.generateAccessToken(testUser);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should generate different access and refresh tokens")
    void testGenerateToken_AccessVsRefresh() {
        // Act
        String accessToken = jwtService.generateAccessToken(testUser);
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Assert
        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    @DisplayName("Should throw exception when user is null")
    void testGenerateToken_NullUser() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            jwtService.generateAccessToken(null)
        );
    }

    // ===================== TOKEN VALIDATION TESTS =====================

    @Test
    @DisplayName("Should validate correct token")
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtService.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtService.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateToken_MalformedToken() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // Act
        boolean isValid = jwtService.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void testValidateToken_InvalidSignature() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        // Tamper with token by changing last character
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        // Act
        boolean isValid = jwtService.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject token with wrong structure")
    void testValidateToken_WrongStructure() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    // ===================== TOKEN EXTRACTION TESTS =====================

    @Test
    @DisplayName("Should extract email from valid token")
    void testExtractEmail_ValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        String email = jwtService.extractEmail(token);

        // Assert
        assertNotNull(email);
        assertEquals(testUser.getEmail(), email);
    }

    @Test
    @DisplayName("Should extract user ID from valid token")
    void testExtractUserId_ValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        Long userId = jwtService.extractUserId(token);

        // Assert
        assertNotNull(userId);
        assertEquals(testUser.getId(), userId);
    }

    @Test
    @DisplayName("Should extract role from valid token")
    void testExtractRole_ValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        String role = jwtService.extractRole(token);

        // Assert
        assertNotNull(role);
        assertEquals(testUser.getRole().name(), role);
    }

    @Test
    @DisplayName("Should extract expiration from valid token")
    void testExtractExpiration_ValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should throw exception when extracting from invalid token")
    void testExtractEmail_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () ->
            jwtService.extractEmail(invalidToken)
        );
    }

    @Test
    @DisplayName("Should extract all claims from token")
    void testExtractAllClaims_ValidToken() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        String email = jwtService.extractEmail(token);
        Long userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        // Assert
        assertEquals(testUser.getEmail(), email);
        assertEquals(testUser.getId(), userId);
        assertEquals(testUser.getRole().name(), role);
    }

    // ===================== TOKEN EXPIRATION TESTS =====================

    @Test
    @DisplayName("Should validate non-expired token")
    void testIsTokenExpired_NotExpired() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act
        boolean isExpired = jwtService.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Should detect expired token")
    void testIsTokenExpired_Expired() {
        // Arrange - Create token with past expiration
        Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
        String expiredToken = jwtService.generateTokenWithExpiration(testUser, Date.from(pastTime));

        // Act
        boolean isExpired = jwtService.isTokenExpired(expiredToken);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    @DisplayName("Should reject expired token during validation")
    void testValidateToken_ExpiredToken() {
        // Arrange
        Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
        String expiredToken = jwtService.generateTokenWithExpiration(testUser, Date.from(pastTime));

        // Act
        boolean isValid = jwtService.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should have correct expiration time for access token")
    void testAccessToken_ExpirationTime() {
        // Arrange
        Instant beforeGeneration = Instant.now();
        String token = jwtService.generateAccessToken(testUser);
        Instant afterGeneration = Instant.now();

        // Act
        Date expiration = jwtService.extractExpiration(token);
        long expirationMillis = expiration.getTime() - System.currentTimeMillis();

        // Assert - Access token should expire in about 1 hour (3600000 ms)
        assertTrue(expirationMillis > 3500000 && expirationMillis < 3700000);
    }

    @Test
    @DisplayName("Should have correct expiration time for refresh token")
    void testRefreshToken_ExpirationTime() {
        // Arrange
        String token = jwtService.generateRefreshToken(testUser);

        // Act
        Date expiration = jwtService.extractExpiration(token);
        long expirationMillis = expiration.getTime() - System.currentTimeMillis();

        // Assert - Refresh token should expire in about 24 hours (86400000 ms)
        assertTrue(expirationMillis > 86000000 && expirationMillis < 87000000);
    }

    // ===================== TOKEN INVALIDATION TESTS =====================

    @Test
    @DisplayName("Should invalidate token successfully")
    void testInvalidateToken_Success() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        assertTrue(jwtService.validateToken(token));

        // Act
        boolean invalidated = jwtService.invalidateToken(token);

        // Assert
        assertTrue(invalidated);
        assertFalse(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Should handle invalidation of already invalid token")
    void testInvalidateToken_AlreadyInvalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean invalidated = jwtService.invalidateToken(invalidToken);

        // Assert
        assertFalse(invalidated);
    }

    @Test
    @DisplayName("Should not allow reuse of invalidated token")
    void testInvalidatedToken_CannotReuse() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        jwtService.invalidateToken(token);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    // ===================== TOKEN REFRESH TESTS =====================

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshAccessToken_Success() {
        // Arrange
        String oldToken = jwtService.generateAccessToken(testUser);

        // Act
        String newToken = jwtService.refreshAccessToken(oldToken);

        // Assert
        assertNotNull(newToken);
        assertNotEquals(oldToken, newToken);
        assertTrue(jwtService.validateToken(newToken));
        assertEquals(testUser.getEmail(), jwtService.extractEmail(newToken));
    }

    @Test
    @DisplayName("Should not refresh invalid token")
    void testRefreshAccessToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () ->
            jwtService.refreshAccessToken(invalidToken)
        );
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    @DisplayName("Should handle user with special characters in email")
    void testGenerateToken_SpecialCharactersInEmail() {
        // Arrange
        testUser.setEmail("test+special@example.com");

        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertTrue(jwtService.validateToken(token));
        assertEquals("test+special@example.com", jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("Should handle user with long name")
    void testGenerateToken_LongName() {
        // Arrange
        testUser.setFirstName("VeryLongFirstNameThatExceedsNormalLength");
        testUser.setLastName("VeryLongLastNameThatExceedsNormalLength");

        // Act
        String token = jwtService.generateAccessToken(testUser);

        // Assert
        assertTrue(jwtService.validateToken(token));
        assertNotNull(jwtService.extractEmail(token));
    }

    @Test
    @DisplayName("Should handle concurrent token generation")
    void testGenerateToken_Concurrent() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        String[] tokens = new String[threadCount];
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                tokens[index] = jwtService.generateAccessToken(testUser);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(tokens[i]);
            assertTrue(jwtService.validateToken(tokens[i]));

            // Ensure all tokens are unique
            for (int j = i + 1; j < threadCount; j++) {
                assertNotEquals(tokens[i], tokens[j]);
            }
        }
    }

    @Test
    @DisplayName("Should handle different user roles")
    void testGenerateToken_DifferentRoles() {
        // Test each role
        for (Role role : Role.values()) {
            testUser.setRole(role);
            String token = jwtService.generateAccessToken(testUser);

            assertTrue(jwtService.validateToken(token));
            assertEquals(role.name(), jwtService.extractRole(token));
        }
    }

    @Test
    @DisplayName("Should maintain token integrity after multiple validations")
    void testValidateToken_MultipleValidations() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);

        // Act & Assert - Validate multiple times
        for (int i = 0; i < 100; i++) {
            assertTrue(jwtService.validateToken(token));
        }
    }

    @Test
    @DisplayName("Should handle token with minimum valid expiration")
    void testGenerateToken_MinimumExpiration() {
        // Arrange
        Instant nearFuture = Instant.now().plus(1, ChronoUnit.SECONDS);

        // Act
        String token = jwtService.generateTokenWithExpiration(testUser, Date.from(nearFuture));

        // Assert
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Should handle token extraction performance")
    void testExtractClaims_Performance() {
        // Arrange
        String token = jwtService.generateAccessToken(testUser);
        long startTime = System.currentTimeMillis();

        // Act - Extract claims multiple times
        for (int i = 0; i < 1000; i++) {
            jwtService.extractEmail(token);
            jwtService.extractUserId(token);
            jwtService.extractRole(token);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert - Should complete in reasonable time (less than 1 second)
        assertTrue(duration < 1000, "Token extraction took too long: " + duration + "ms");
    }
}
