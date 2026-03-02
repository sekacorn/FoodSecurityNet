package com.foodsec.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing MFA (Multi-Factor Authentication) backup codes.
 *
 * Backup codes provide an alternative authentication method when users
 * cannot access their primary MFA device (e.g., TOTP authenticator app).
 * Each code is single-use and becomes invalid once used.
 */
@Entity
@Table(name = "mfa_backup_codes", indexes = {
    @Index(name = "idx_backup_code_user", columnList = "user_id"),
    @Index(name = "idx_backup_code_hash", columnList = "code_hash")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaBackupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_backup_code_user"))
    private User user;

    @Column(name = "code_hash", nullable = false, length = 60)
    private String codeHash;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Checks if this backup code is valid for use
     *
     * @return true if code is not used and not expired
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Marks this backup code as used
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Checks if this backup code has expired
     *
     * @return true if code has passed its expiration date
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
