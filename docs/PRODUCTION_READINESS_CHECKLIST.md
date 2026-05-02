# Production Readiness Checklist

FoodSecurityNet is currently a prototype / starter kit. Use this checklist before deploying it with real users, real farm data, personal data, institutional data, or community-sensitive data.

This checklist is not a certification and does not replace legal, security, accessibility, privacy, AI, domain, or public-sector review.

## Governance

- Confirm the deployment owner and data owner.
- Confirm who can approve production release.
- Define support expectations and escalation paths.
- Document the intended users and use cases.
- Decide whether the deployment is research, education, nonprofit operations, public-sector service, or another category.

## Licensing

- Review the MIT License in `LICENSE`.
- Review third-party dependency licenses.
- Confirm dataset licenses and attribution requirements.
- Document any external API terms, including AI providers and public data providers.

## Secrets And Configuration

- Replace all demo and default secrets.
- Store secrets in a managed secret store or secure environment system.
- Remove hard-coded demo credentials from any deployed service.
- Configure production CORS origins.
- Configure production OAuth2/SAML providers if used.
- Configure SMTP credentials securely if email/MFA flows are used.
- Review all `.env.example` values before deployment.

## Authentication And Authorization

- Verify registration, login, logout, refresh, profile, and MFA flows.
- Test role and permission boundaries.
- Confirm admin-only endpoints cannot be reached by normal users.
- Review JWT expiration, refresh token behavior, signing keys, and rotation.
- Review SSO account linking and deprovisioning behavior.

## Data Protection

- Classify all stored data.
- Define retention periods for accounts, uploads, sessions, annotations, LLM queries, logs, and audit records.
- Implement deletion and export workflows where required.
- Encrypt backups.
- Restrict database access.
- Review logs for personal data, secrets, and sensitive community information.
- Document backup and restore procedures.

## AI Review

- Identify all AI models and external AI APIs.
- Document training data, synthetic data, evaluation limits, and known failure modes.
- Clearly label AI-assisted outputs in user-facing workflows.
- Define when human review is required.
- Test for harmful, misleading, or overconfident responses.
- Review EU AI Act obligations where relevant.

## Accessibility

- Complete keyboard-only testing across all routes.
- Complete screen-reader testing for navigation, forms, modals, live regions, and error states.
- Validate color contrast against WCAG 2.1 AA.
- Test reduced motion behavior.
- Review focus order and visible focus states.
- Document Section 508 / WCAG evidence if used in US public-sector or education contexts.
- Review European Accessibility Act / EN 301 549 obligations where relevant.

## Security

- Run dependency scans for Java, Node, Python, container images, and OS packages.
- Run secret scanning.
- Review security headers and Content Security Policy.
- Enable TLS and HSTS in production.
- Harden NGINX and ingress configuration.
- Add centralized logging and alerting.
- Define audit log retention.
- Run vulnerability scans before launch.
- Consider penetration testing for public deployments.
- Document incident response.

## Infrastructure

- Validate Docker Compose or Kubernetes manifests in the target environment.
- Resolve service port and routing differences between compose files and service configuration.
- Configure resource limits and health checks.
- Configure database migrations.
- Configure Redis persistence and security.
- Confirm CPU/GPU assumptions for AI services.
- Test service restart and failure behavior.
- Test monitoring dashboards and alerts.

## Compliance Planning

- GDPR: review lawful basis, privacy notices, retention, deletion, access/export requests, processor agreements, and cross-border transfers.
- Section 508 / WCAG 2.1 AA: complete audit and remediation before claiming conformance.
- European Accessibility Act / EN 301 549: review public-facing and public-sector obligations.
- EU AI Act: classify AI use, document risk controls, and define human oversight.
- NIS2: review organizational risk, incident response, supplier management, and operational controls.
- Cyber Resilience Act: review secure development, vulnerability handling, SBOM, and patch processes if distributing as a digital product in the EU.
- NIST SP 800-53 / NIST Cybersecurity Framework: map required controls for public-sector or federally adjacent deployments.
- FedRAMP: do not claim authorization unless the deployment completes the formal process with an authorized cloud boundary and assessment.

## Release Decision

Before launch, document:

- What was tested.
- What remains a known risk.
- Who approved the release.
- How users can report problems.
- How fixes will be shipped.
