package com.foodsec.auth.service;

import com.foodsec.auth.dto.*;
import com.foodsec.auth.exception.AuthException;
import com.foodsec.auth.model.Role;
import com.foodsec.auth.model.SsoProvider;
import com.foodsec.auth.model.User;
import com.foodsec.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Main authentication service handling user registration, login, and authentication flows.
 *
 * Provides comprehensive authentication features including:
 * - User registration and login
 * - Multi-factor authentication
 * - SSO integration
 * - Token management
 * - Account security (rate limiting, lockout)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MfaService mfaService;
    private final EmailService emailService;

    @Value("${auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${auth.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    /**
     * Registers a new user
     *
     * @param request registration request
     * @return the created user
     * @throws AuthException if username or email already exists
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw AuthException.usernameExists();
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.emailExists();
        }

        // Create user entity
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .mbtiType(request.getMbtiType())
            .role(request.getRole() != null ? Role.valueOf(request.getRole()) : Role.USER)
            .ssoProvider(SsoProvider.LOCAL)
            .mfaEnabled(false)
            .emailVerified(false)
            .isActive(true)
            .failedLoginAttempts(0)
            .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        // Send welcome and verification emails
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        // TODO: Generate and send verification token
        // emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);

        return user;
    }

    /**
     * Authenticates a user with username/password
     *
     * @param request login request
     * @return login response with tokens or MFA requirement
     * @throws AuthException if credentials are invalid or account is locked
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(
            request.getUsernameOrEmail(),
            request.getUsernameOrEmail()
        ).orElseThrow(AuthException::invalidCredentials);

        // Check if account can authenticate
        if (!user.canAuthenticate()) {
            if (user.isLocked()) {
                throw AuthException.accountLocked(
                    String.format("Account is locked until %s due to multiple failed login attempts",
                        user.getLockedUntil())
                );
            }
            throw AuthException.accountInactive();
        }

        // Verify password using constant-time comparison
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw AuthException.invalidCredentials();
        }

        // Reset failed login attempts on successful password verification
        user.resetFailedLoginAttempts();

        // Check if MFA is required
        if (user.getMfaEnabled()) {
            // If MFA code or backup code provided, verify it
            if (request.getMfaCode() != null || request.getBackupCode() != null) {
                boolean mfaValid = false;

                if (request.getMfaCode() != null) {
                    mfaValid = mfaService.verifyCode(user.getMfaSecret(), request.getMfaCode());
                } else if (request.getBackupCode() != null) {
                    mfaValid = mfaService.verifyBackupCode(user, request.getBackupCode());
                }

                if (!mfaValid) {
                    throw AuthException.mfaVerificationFailed();
                }

                // MFA verified, proceed with login
                return completeLogin(user, request.isRememberMe());
            } else {
                // MFA required but not provided
                return LoginResponse.mfaRequired(user.getId(), true);
            }
        }

        // No MFA required, complete login
        return completeLogin(user, request.isRememberMe());
    }

    /**
     * Completes the login process and generates tokens
     *
     * @param user the authenticated user
     * @param rememberMe whether to extend token lifetime
     * @return login response with tokens
     */
    private LoginResponse completeLogin(User user, boolean rememberMe) {
        user.updateLastLogin();
        userRepository.save(user);

        // Generate tokens with extended lifetime if rememberMe is true
        long tokenExpiration = rememberMe ? 60 * 24 * 7 : jwtService.getAccessTokenExpirationSeconds() / 60; // 7 days or default
        String accessToken = jwtService.generateAccessToken(user, tokenExpiration);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenExpiration * 60) // Convert to seconds
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .mbtiType(user.getMbtiType())
            .mfaEnabled(user.getMfaEnabled())
            .mfaRequired(false)
            .emailVerified(user.getEmailVerified())
            .build();
    }

    /**
     * Handles SSO login
     *
     * @param provider SSO provider
     * @param ssoSubject subject ID from SSO provider
     * @param email email from SSO provider
     * @param fullName full name from SSO provider
     * @return login response with tokens
     */
    @Transactional
    public LoginResponse loginWithSSO(SsoProvider provider, String ssoSubject, String email, String fullName) {
        log.info("SSO login attempt with provider: {}, subject: {}", provider, ssoSubject);

        // Find or create user
        User user = userRepository.findBySsoProviderAndSsoSubject(provider, ssoSubject)
            .orElseGet(() -> {
                // Create new user from SSO
                String username = generateUsernameFromEmail(email);
                User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .fullName(fullName)
                    .role(Role.USER)
                    .ssoProvider(provider)
                    .ssoSubject(ssoSubject)
                    .emailVerified(true) // SSO providers verify emails
                    .isActive(true)
                    .mfaEnabled(false)
                    .build();

                newUser = userRepository.save(newUser);
                log.info("New user created from SSO: {}", newUser.getUsername());
                emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getUsername());
                return newUser;
            });

        // Check if account is active
        if (!user.getIsActive()) {
            throw AuthException.accountInactive();
        }

        return completeLogin(user, false);
    }

    /**
     * Refreshes access token using refresh token
     *
     * @param refreshToken the refresh token
     * @return login response with new access token
     * @throws AuthException if refresh token is invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(String refreshToken) {
        log.debug("Refreshing access token");

        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw AuthException.invalidToken();
        }

        UUID userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        if (!user.canAuthenticate()) {
            throw AuthException.accountInactive();
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpirationSeconds())
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .mbtiType(user.getMbtiType())
            .mfaEnabled(user.getMfaEnabled())
            .emailVerified(user.getEmailVerified())
            .build();
    }

    /**
     * Gets user information from token
     *
     * @param token the access token
     * @return user DTO
     */
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String token) {
        UUID userId = jwtService.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
            .orElseThrow(AuthException::invalidToken);

        return convertToDto(user);
    }

    /**
     * Handles failed login attempts and account lockout
     *
     * @param user the user with failed login
     */
    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
            user.lockAccount(lockoutDurationMinutes);
            log.warn("Account locked due to failed login attempts: {}", user.getUsername());

            emailService.sendSecurityAlert(
                user.getEmail(),
                user.getUsername(),
                String.format("Your account has been locked for %d minutes due to multiple failed login attempts.", lockoutDurationMinutes)
            );
        }

        userRepository.save(user);
    }

    /**
     * Generates a unique username from email
     *
     * @param email the email address
     * @return generated username
     */
    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    /**
     * Converts User entity to UserDto
     *
     * @param user the user entity
     * @return user DTO
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .mbtiType(user.getMbtiType())
            .ssoProvider(user.getSsoProvider())
            .mfaEnabled(user.getMfaEnabled())
            .emailVerified(user.getEmailVerified())
            .isActive(user.getIsActive())
            .lastLogin(user.getLastLogin())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
