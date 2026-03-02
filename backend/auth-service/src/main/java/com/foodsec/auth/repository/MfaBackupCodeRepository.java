package com.foodsec.auth.repository;

import com.foodsec.auth.model.MfaBackupCode;
import com.foodsec.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository interface for MfaBackupCode entity operations.
 *
 * Provides data access methods for managing MFA backup codes including:
 * - Code validation and usage tracking
 * - User-specific code management
 * - Expiration handling
 */
@Repository
public interface MfaBackupCodeRepository extends JpaRepository<MfaBackupCode, UUID> {

    /**
     * Finds all backup codes for a specific user
     *
     * @param user the user to find codes for
     * @return list of backup codes belonging to the user
     */
    List<MfaBackupCode> findByUser(User user);

    /**
     * Finds all valid (unused and unexpired) backup codes for a user
     *
     * @param user the user to find codes for
     * @param now current timestamp for expiration check
     * @return list of valid backup codes
     */
    @Query("SELECT m FROM MfaBackupCode m WHERE m.user = :user AND m.used = false AND m.expiresAt > :now")
    List<MfaBackupCode> findValidCodesByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Finds a backup code by its hash
     *
     * @param codeHash the hashed backup code
     * @return Optional containing the backup code if found
     */
    Optional<MfaBackupCode> findByCodeHash(String codeHash);

    /**
     * Counts valid backup codes for a user
     *
     * @param user the user to count codes for
     * @param now current timestamp for expiration check
     * @return number of valid backup codes
     */
    @Query("SELECT COUNT(m) FROM MfaBackupCode m WHERE m.user = :user AND m.used = false AND m.expiresAt > :now")
    long countValidCodesByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Finds all used backup codes for a user
     *
     * @param user the user to find codes for
     * @return list of used backup codes
     */
    List<MfaBackupCode> findByUserAndUsedTrue(User user);

    /**
     * Finds all expired backup codes
     *
     * @param now current timestamp for expiration check
     * @return list of expired backup codes
     */
    @Query("SELECT m FROM MfaBackupCode m WHERE m.expiresAt <= :now")
    List<MfaBackupCode> findExpiredCodes(@Param("now") LocalDateTime now);

    /**
     * Deletes all backup codes for a specific user
     *
     * @param user the user whose codes should be deleted
     */
    @Modifying
    @Query("DELETE FROM MfaBackupCode m WHERE m.user = :user")
    void deleteByUser(@Param("user") User user);

    /**
     * Deletes all expired backup codes
     *
     * @param now current timestamp for expiration check
     * @return number of deleted codes
     */
    @Modifying
    @Query("DELETE FROM MfaBackupCode m WHERE m.expiresAt <= :now")
    int deleteExpiredCodes(@Param("now") LocalDateTime now);

    /**
     * Deletes all used backup codes for a user
     *
     * @param user the user whose used codes should be deleted
     */
    @Modifying
    @Query("DELETE FROM MfaBackupCode m WHERE m.user = :user AND m.used = true")
    void deleteUsedCodesByUser(@Param("user") User user);
}
