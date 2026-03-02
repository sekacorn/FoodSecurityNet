package com.foodsec.auth;

import com.foodsec.auth.dto.*;
import com.foodsec.auth.exception.AuthException;
import com.foodsec.auth.model.Role;
import com.foodsec.auth.model.User;
import com.foodsec.auth.repository.UserRepository;
import com.foodsec.auth.service.AuthService;
import com.foodsec.auth.service.EmailService;
import com.foodsec.auth.service.JwtService;
import com.foodsec.auth.service.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests registration, login, SSO, and MFA functionality
 * Coverage target: >90%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private MfaService mfaService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("SecurePass123!");
        validRegisterRequest.setFirstName("John");
        validRegisterRequest.setLastName("Doe");
        validRegisterRequest.setOrganization("TestOrg");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("SecurePass123!");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.RESEARCHER);
        testUser.setMfaEnabled(false);
        testUser.setEmailVerified(true);
        testUser.setActive(true);
    }

    // ===================== REGISTRATION TESTS =====================

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        UserDto result = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRegisterRequest.getEmail(), result.getEmail());
        assertEquals(validRegisterRequest.getFirstName(), result.getFirstName());
        verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(true);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.register(validRegisterRequest)
        );
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).existsByEmail(validRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when password is weak")
    void testRegisterUser_WeakPassword() {
        // Arrange
        validRegisterRequest.setPassword("weak");

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.register(validRegisterRequest)
        );
        assertTrue(exception.getMessage().contains("Password does not meet requirements"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email format is invalid")
    void testRegisterUser_InvalidEmailFormat() {
        // Arrange
        validRegisterRequest.setEmail("invalid-email");

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.register(validRegisterRequest)
        );
        assertTrue(exception.getMessage().contains("Invalid email format"));
        verify(userRepository, never()).save(any(User.class));
    }

    // ===================== LOGIN TESTS =====================

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        // Act
        LoginResponse result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertFalse(result.isMfaRequired());
        assertNotNull(result.getUser());
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Should return MFA required when user has MFA enabled")
    void testLogin_MfaRequired() {
        // Arrange
        testUser.setMfaEnabled(true);
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // Act
        LoginResponse result = authService.login(validLoginRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isMfaRequired());
        assertNull(result.getAccessToken());
        assertNull(result.getRefreshToken());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.login(validLoginRequest)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testLogin_IncorrectPassword() {
        // Arrange
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.login(validLoginRequest)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when account is not verified")
    void testLogin_AccountNotVerified() {
        // Arrange
        testUser.setEmailVerified(false);
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.login(validLoginRequest)
        );
        assertEquals("Email not verified", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when account is inactive")
    void testLogin_AccountInactive() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.login(validLoginRequest)
        );
        assertEquals("Account is disabled", exception.getMessage());
    }

    // ===================== MFA VERIFICATION TESTS =====================

    @Test
    @DisplayName("Should successfully verify MFA code and return tokens")
    void testVerifyMfa_Success() {
        // Arrange
        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setEmail("test@example.com");
        mfaRequest.setCode("123456");

        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("SECRET");

        when(userRepository.findByEmail(mfaRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(mfaService.verifyTotp(testUser.getMfaSecret(), mfaRequest.getCode())).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        // Act
        LoginResponse result = authService.verifyMfa(mfaRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertFalse(result.isMfaRequired());
        verify(mfaService).verifyTotp(testUser.getMfaSecret(), mfaRequest.getCode());
    }

    @Test
    @DisplayName("Should throw exception when MFA code is invalid")
    void testVerifyMfa_InvalidCode() {
        // Arrange
        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setEmail("test@example.com");
        mfaRequest.setCode("000000");

        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("SECRET");

        when(userRepository.findByEmail(mfaRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(mfaService.verifyTotp(testUser.getMfaSecret(), mfaRequest.getCode())).thenReturn(false);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.verifyMfa(mfaRequest)
        );
        assertEquals("Invalid MFA code", exception.getMessage());
        verify(jwtService, never()).generateAccessToken(any());
    }

    // ===================== TOKEN REFRESH TESTS =====================

    @Test
    @DisplayName("Should successfully refresh access token")
    void testRefreshToken_Success() {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        when(jwtService.validateToken(refreshRequest.getRefreshToken())).thenReturn(true);
        when(jwtService.extractEmail(refreshRequest.getRefreshToken())).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");

        // Act
        String result = authService.refreshToken(refreshRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new-access-token", result);
        verify(jwtService).validateToken(refreshRequest.getRefreshToken());
        verify(jwtService).generateAccessToken(testUser);
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void testRefreshToken_InvalidToken() {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid-token");

        when(jwtService.validateToken(refreshRequest.getRefreshToken())).thenReturn(false);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.refreshToken(refreshRequest)
        );
        assertEquals("Invalid refresh token", exception.getMessage());
        verify(jwtService, never()).generateAccessToken(any());
    }

    // ===================== EMAIL VERIFICATION TESTS =====================

    @Test
    @DisplayName("Should successfully verify email")
    void testVerifyEmail_Success() {
        // Arrange
        String token = "valid-verification-token";
        testUser.setEmailVerified(false);

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = authService.verifyEmail(token);

        // Assert
        assertTrue(result);
        verify(userRepository).save(argThat(user -> user.isEmailVerified()));
    }

    @Test
    @DisplayName("Should throw exception when verification token is invalid")
    void testVerifyEmail_InvalidToken() {
        // Arrange
        String token = "invalid-token";

        when(jwtService.validateToken(token)).thenReturn(false);

        // Act & Assert
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.verifyEmail(token)
        );
        assertEquals("Invalid verification token", exception.getMessage());
    }

    // ===================== PASSWORD RESET TESTS =====================

    @Test
    @DisplayName("Should successfully initiate password reset")
    void testInitiatePasswordReset_Success() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // Act
        authService.initiatePasswordReset(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(emailService).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("Should not throw exception when user not found for password reset")
    void testInitiatePasswordReset_UserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert - Should not throw exception for security reasons
        assertDoesNotThrow(() -> authService.initiatePasswordReset(email));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should successfully reset password")
    void testResetPassword_Success() {
        // Arrange
        String token = "valid-reset-token";
        String newPassword = "NewSecurePass123!";

        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = authService.resetPassword(token, newPassword);

        // Assert
        assertTrue(result);
        verify(userRepository).save(argThat(user ->
            user.getPassword().equals("$2a$10$newHashedPassword")
        ));
    }

    // ===================== LOGOUT TESTS =====================

    @Test
    @DisplayName("Should successfully logout user")
    void testLogout_Success() {
        // Arrange
        String token = "valid-access-token";
        when(jwtService.invalidateToken(token)).thenReturn(true);

        // Act
        boolean result = authService.logout(token);

        // Assert
        assertTrue(result);
        verify(jwtService).invalidateToken(token);
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    @DisplayName("Should handle null input gracefully in registration")
    void testRegister_NullInput() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            authService.register(null)
        );
    }

    @Test
    @DisplayName("Should handle concurrent registration attempts")
    void testRegister_ConcurrentAttempts() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.getEmail()))
            .thenReturn(false)
            .thenReturn(true);

        // Act - First attempt should succeed, second should fail
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto firstAttempt = authService.register(validRegisterRequest);
        assertNotNull(firstAttempt);

        // Second attempt should throw exception
        assertThrows(AuthException.class, () ->
            authService.register(validRegisterRequest)
        );
    }

    @Test
    @DisplayName("Should handle database errors during registration")
    void testRegister_DatabaseError() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            authService.register(validRegisterRequest)
        );
    }

    @Test
    @DisplayName("Should handle email service failure gracefully")
    void testRegister_EmailServiceFailure() {
        // Arrange
        when(userRepository.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doThrow(new RuntimeException("Email service down"))
            .when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act & Assert - Should still save user even if email fails
        UserDto result = authService.register(validRegisterRequest);
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }
}
