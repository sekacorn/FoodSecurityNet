package com.foodsec.auth.model;

/**
 * Enumeration representing supported Single Sign-On (SSO) providers.
 *
 * Supported Providers:
 * - GOOGLE: Google OAuth2 authentication
 * - MICROSOFT: Microsoft Azure AD authentication
 * - OKTA: Okta identity platform
 * - SAML: Generic SAML 2.0 providers
 * - LOCAL: Traditional username/password authentication
 */
public enum SsoProvider {
    /**
     * Google OAuth2 authentication provider
     */
    GOOGLE("google"),

    /**
     * Microsoft Azure AD authentication provider
     */
    MICROSOFT("microsoft"),

    /**
     * Okta identity platform
     */
    OKTA("okta"),

    /**
     * Generic SAML 2.0 provider
     */
    SAML("saml"),

    /**
     * Local authentication (username/password)
     */
    LOCAL("local");

    private final String providerId;

    SsoProvider(String providerId) {
        this.providerId = providerId;
    }

    /**
     * Gets the provider identifier used in OAuth2 flows
     *
     * @return provider identifier string
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Checks if this provider requires external authentication
     *
     * @return true if provider is not LOCAL
     */
    public boolean isExternal() {
        return this != LOCAL;
    }

    /**
     * Converts a provider ID string to the corresponding enum value
     *
     * @param providerId the provider identifier
     * @return the matching SsoProvider enum
     * @throws IllegalArgumentException if provider ID is not recognized
     */
    public static SsoProvider fromProviderId(String providerId) {
        for (SsoProvider provider : values()) {
            if (provider.providerId.equalsIgnoreCase(providerId)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown SSO provider: " + providerId);
    }
}
