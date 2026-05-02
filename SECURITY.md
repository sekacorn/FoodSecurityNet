# Security Policy

FoodSecurityNet is a prototype / starter kit for public-interest food security software. It includes security-oriented controls, but it has not completed a production security audit.

## Reporting Security Issues

If you find a vulnerability, please report it privately before opening a public issue.

Contact information available in this repository:

```text
sekacorn@gmail.com
```

Include:

- A short description of the issue.
- The affected file, service, endpoint, or configuration.
- Steps to reproduce, if safe to share.
- Any suggested mitigation.

Do not include real personal data, private farm data, secrets, API keys, or institutional credentials in a report.

## Current Security Posture

The repository includes:

- JWT-oriented authentication and API gateway filtering.
- MFA-oriented auth flows.
- OAuth2 and SAML-oriented configuration.
- Redis-backed rate limiting in the API gateway.
- Baseline security headers in gateway/auth code.
- Health, metrics, and Prometheus-style endpoints.
- PostgreSQL schema support for audit logs.
- Docker, NGINX, and Kubernetes deployment examples.

The repository also includes demo and prototype behavior:

- `demo-server.js` uses demo credentials and a hard-coded demo JWT secret.
- Some services include default local passwords or placeholder secrets in example configuration.
- The Java LLM service can return mock responses when no API key is configured.
- Some visualization paths are placeholders.
- No SBOM, dependency scanning, secret scanning, penetration test report, or formal threat model is included.

## Deployment Responsibilities

Before using FoodSecurityNet with real users or sensitive data, deployers should review and implement:

- Strong secrets in environment variables or a managed secret store.
- TLS termination and HSTS behavior for deployed domains.
- Strict production CORS origins.
- Authentication and authorization tests for every protected workflow.
- Dependency and container image scanning.
- Secret scanning in CI.
- Centralized logs, audit log retention, and alerting.
- Backup and restore procedures for PostgreSQL and Redis.
- A vulnerability disclosure and patch process.
- Incident response procedures.
- Review of AI, privacy, accessibility, and public-sector obligations.

## Supported Versions

No formal version support policy exists yet. Treat the current repository state as the active development version.
