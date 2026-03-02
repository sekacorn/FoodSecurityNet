package com.foodsec.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsec.auth.controller.AuthController;
import com.foodsec.auth.dto.*;
import com.foodsec.auth.exception.AuthException;
import com.foodsec.auth.model.Role;
import com.foodsec.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 * Tests REST endpoint behavior
 * Coverage target: >90%
 */
@WebMvcTest(AuthController.class)
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserDto userDto;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("SecurePass123!");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setRole(Role.RESEARCHER);

        loginResponse = new LoginResponse();
        loginResponse.setAccessToken("access-token");
        loginResponse.setRefreshToken("refresh-token");
        loginResponse.setUser(userDto);
        loginResponse.setMfaRequired(false);
    }

    // ===================== REGISTRATION ENDPOINT TESTS =====================

    @Test
    @DisplayName("POST /api/auth/register - Should register user successfully")
    void testRegisterEndpoint_Success() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for invalid request")
    void testRegisterEndpoint_InvalidRequest() throws Exception {
        // Arrange
        registerRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 409 for duplicate email")
    void testRegisterEndpoint_DuplicateEmail() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new AuthException("Email already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    // ===================== LOGIN ENDPOINT TESTS =====================

    @Test
    @DisplayName("POST /api/auth/login - Should login successfully")
    void testLoginEndpoint_Success() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.mfaRequired").value(false));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return MFA required")
    void testLoginEndpoint_MfaRequired() throws Exception {
        // Arrange
        loginResponse.setMfaRequired(true);
        loginResponse.setAccessToken(null);
        loginResponse.setRefreshToken(null);
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mfaRequired").value(true))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 for invalid credentials")
    void testLoginEndpoint_InvalidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ===================== MFA VERIFICATION ENDPOINT TESTS =====================

    @Test
    @DisplayName("POST /api/auth/mfa/verify - Should verify MFA successfully")
    void testVerifyMfaEndpoint_Success() throws Exception {
        // Arrange
        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setEmail("test@example.com");
        mfaRequest.setCode("123456");
        when(authService.verifyMfa(any(MfaVerifyRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/mfa/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));

        verify(authService).verifyMfa(any(MfaVerifyRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/mfa/verify - Should return 401 for invalid code")
    void testVerifyMfaEndpoint_InvalidCode() throws Exception {
        // Arrange
        MfaVerifyRequest mfaRequest = new MfaVerifyRequest();
        mfaRequest.setEmail("test@example.com");
        mfaRequest.setCode("000000");
        when(authService.verifyMfa(any(MfaVerifyRequest.class)))
                .thenThrow(new AuthException("Invalid MFA code"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/mfa/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mfaRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ===================== TOKEN REFRESH ENDPOINT TESTS =====================

    @Test
    @DisplayName("POST /api/auth/refresh - Should refresh token successfully")
    @WithMockUser
    void testRefreshTokenEndpoint_Success() throws Exception {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");
        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn("new-access-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }

    // ===================== LOGOUT ENDPOINT TESTS =====================

    @Test
    @DisplayName("POST /api/auth/logout - Should logout successfully")
    @WithMockUser
    void testLogoutEndpoint_Success() throws Exception {
        // Arrange
        when(authService.logout(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());

        verify(authService).logout(anyString());
    }

    // ===================== EMAIL VERIFICATION ENDPOINT TESTS =====================

    @Test
    @DisplayName("GET /api/auth/verify-email - Should verify email successfully")
    void testVerifyEmailEndpoint_Success() throws Exception {
        // Arrange
        when(authService.verifyEmail(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", "valid-token"))
                .andExpect(status().isOk());

        verify(authService).verifyEmail(anyString());
    }

    // ===================== PASSWORD RESET ENDPOINT TESTS =====================

    @Test
    @DisplayName("POST /api/auth/forgot-password - Should initiate password reset")
    void testForgotPasswordEndpoint_Success() throws Exception {
        // Arrange
        doNothing().when(authService).initiatePasswordReset(anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .param("email", "test@example.com"))
                .andExpect(status().isOk());

        verify(authService).initiatePasswordReset("test@example.com");
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Should reset password successfully")
    void testResetPasswordEndpoint_Success() throws Exception {
        // Arrange
        when(authService.resetPassword(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .param("token", "valid-token")
                .param("password", "NewSecurePass123!"))
                .andExpect(status().isOk());

        verify(authService).resetPassword(anyString(), anyString());
    }

    // ===================== VALIDATION TESTS =====================

    @Test
    @DisplayName("Should validate request body format")
    void testValidation_MalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate required fields")
    void testValidation_MissingFields() throws Exception {
        // Arrange
        registerRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    // ===================== SECURITY TESTS =====================

    @Test
    @DisplayName("Should require CSRF token for POST requests")
    void testSecurity_CsrfRequired() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should accept correct Content-Type")
    void testContentType_Json() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should reject incorrect Content-Type")
    void testContentType_Invalid() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
