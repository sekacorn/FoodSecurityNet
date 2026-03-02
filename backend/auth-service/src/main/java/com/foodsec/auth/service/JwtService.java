package com.foodsec.auth.service;

import com.foodsec.auth.exception.UnauthorizedException;
import com.foodsec.auth.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for JWT token generation, validation, and management.
 *
 * Provides methods to:
 * - Generate access and refresh tokens
 * - Validate and parse JWT tokens
 * - Extract claims from tokens
 * - Handle token expiration
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;
    private final String issuer;

    public JwtService(
        @Value("${jwt.secret:your-256-bit-secret-key-change-this-in-production-please-make-it-long-enough}") String secret,
        @Value("${jwt.access-token-expiration-minutes:15}") long accessTokenExpirationMinutes,
        @Value("${jwt.refresh-token-expiration-days:30}") long refreshTokenExpirationDays,
        @Value("${jwt.issuer:foodsecuritynet-auth}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.issuer = issuer;
    }

    /**
     * Generates an access token for a user
     *
     * @param user the user to generate token for
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        return generateAccessToken(user, accessTokenExpirationMinutes);
    }

    /**
     * Generates an access token with custom expiration
     *
     * @param user the user to generate token for
     * @param expirationMinutes token expiration in minutes
     * @return JWT access token
     */
    public String generateAccessToken(User user, long expirationMinutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("mfaEnabled", user.getMfaEnabled());
        claims.put("emailVerified", user.getEmailVerified());
        claims.put("tokenType", "ACCESS");

        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId().toString())
            .setIssuer(issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Generates a refresh token for a user
     *
     * @param user the user to generate token for
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("tokenType", "REFRESH");

        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId().toString())
            .setIssuer(issuer)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Validates a JWT token and returns claims if valid
     *
     * @param token the JWT token to validate
     * @return claims from the token
     * @throws UnauthorizedException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw UnauthorizedException.invalidToken();
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw UnauthorizedException.invalidToken();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw UnauthorizedException.sessionExpired();
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw UnauthorizedException.invalidToken();
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw UnauthorizedException.invalidToken();
        }
    }

    /**
     * Extracts user ID from token
     *
     * @param token the JWT token
     * @return user ID
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        String userId = claims.get("userId", String.class);
        return UUID.fromString(userId);
    }

    /**
     * Extracts username from token
     *
     * @param token the JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("username", String.class);
    }

    /**
     * Checks if token is expired
     *
     * @param token the JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Checks if token is a refresh token
     *
     * @param token the JWT token
     * @return true if token is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = validateToken(token);
            String tokenType = claims.get("tokenType", String.class);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the expiration time in seconds for access tokens
     *
     * @return expiration time in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60;
    }

    /**
     * Extracts the token from the Authorization header
     *
     * @param authorizationHeader the Authorization header value
     * @return the JWT token without the Bearer prefix
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
