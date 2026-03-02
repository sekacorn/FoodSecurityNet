package com.foodsec.auth.model;

/**
 * Enumeration representing user roles in the FoodSecurityNet platform.
 *
 * Role Hierarchy:
 * - USER: Standard user with basic permissions
 * - MODERATOR: Can moderate content and assist users
 * - ADMIN: Full administrative access to the platform
 * - ENTERPRISE: Business/organization accounts with extended features
 */
public enum Role {
    /**
     * Standard user role with basic platform access
     */
    USER,

    /**
     * Moderator role with content moderation capabilities
     */
    MODERATOR,

    /**
     * Administrator role with full platform access
     */
    ADMIN,

    /**
     * Enterprise user role for business/organization accounts
     */
    ENTERPRISE;

    /**
     * Checks if this role has administrative privileges
     *
     * @return true if role is ADMIN or higher
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Checks if this role has moderation privileges
     *
     * @return true if role is MODERATOR or higher
     */
    public boolean canModerate() {
        return this == MODERATOR || this == ADMIN;
    }

    /**
     * Checks if this role has enterprise features
     *
     * @return true if role is ENTERPRISE
     */
    public boolean isEnterprise() {
        return this == ENTERPRISE;
    }
}
