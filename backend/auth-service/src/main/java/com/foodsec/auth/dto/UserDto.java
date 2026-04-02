package com.foodsec.auth.dto;

import com.foodsec.auth.model.Role;
import com.foodsec.auth.model.SsoProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for user information.
 *
 * Used to transfer user data without exposing sensitive information
 * like password hashes or MFA secrets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private SsoProvider ssoProvider;
    private boolean mfaEnabled;
    private boolean emailVerified;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
