package com.foodsec.auth.repository;

import com.foodsec.auth.model.SsoProvider;
import com.foodsec.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository interface for User entity operations.
 *
 * Provides data access methods for user management including:
 * - Standard CRUD operations
 * - User lookup by various identifiers
 * - SSO integration queries
 * - Account status management
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by username
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email address
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by username or email
     *
     * @param username the username to search for
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Finds a user by SSO provider and subject ID
     *
     * @param ssoProvider the SSO provider
     * @param ssoSubject the subject ID from the SSO provider
     * @return Optional containing the user if found
     */
    Optional<User> findBySsoProviderAndSsoSubject(SsoProvider ssoProvider, String ssoSubject);

    /**
     * Checks if a username already exists
     *
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email already exists
     *
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given SSO provider and subject
     *
     * @param ssoProvider the SSO provider
     * @param ssoSubject the subject ID from the SSO provider
     * @return true if user exists
     */
    boolean existsBySsoProviderAndSsoSubject(SsoProvider ssoProvider, String ssoSubject);

    /**
     * Finds all active users
     *
     * @return list of active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Finds all users with MFA enabled
     *
     * @return list of users with MFA enabled
     */
    List<User> findByMfaEnabledTrue();

    /**
     * Finds all locked users
     *
     * @param now current timestamp to compare against
     * @return list of locked users
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Finds users whose account lock has expired
     *
     * @param now current timestamp to compare against
     * @return list of users whose lock has expired
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    List<User> findExpiredLocks(@Param("now") LocalDateTime now);

    /**
     * Finds users who have not verified their email
     *
     * @return list of users with unverified emails
     */
    List<User> findByEmailVerifiedFalse();

    /**
     * Counts users by role
     *
     * @param role the role to count
     * @return number of users with the specified role
     */
    long countByRole(String role);

    /**
     * Finds users created after a specific date
     *
     * @param date the date to compare against
     * @return list of users created after the date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
}
