# Compliance Readiness

This repository is being prepared for external accessibility and security review. The goal is compliance readiness and audit friendliness, not certification or a legal claim of full conformance.

## Scope

This document maps implemented controls and known gaps to the current codebase while preserving core product capabilities:

- 3D exploration
- AI-assisted analysis and troubleshooting
- collaboration sessions and live updates
- MFA and SSO-oriented auth flows
- export tools
- local mock/demo workflows

## Target Readiness Areas

- Section 508
- WCAG 2.1 AA, with progress toward WCAG 2.2 AA
- OWASP ASVS Level 2
- NIST Cybersecurity Framework 2.0
- Selected NIST SP 800-53 and NIST SP 800-63 controls relevant to web application security and identity flows

## Current Control Mapping

### Accessibility

| Area | Current Implementation | Evidence |
|---|---|---|
| Skip navigation | Skip link allows keyboard users to bypass repeated navigation | [`frontend/src/App.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/App.jsx), [`frontend/src/index.css`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/index.css) |
| Screen reader route updates | Route announcer provides page-change feedback for assistive tech | [`frontend/src/components/RouteAnnouncer.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/RouteAnnouncer.jsx), [`frontend/src/App.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/App.jsx) |
| Navigation semantics | Nav landmarks, labels, and active-page indication are exposed to assistive tech | [`frontend/src/components/Navbar.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/Navbar.jsx) |
| Reduced motion support | Motion-sensitive users can avoid non-essential animation | [`frontend/src/index.css`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/index.css) |
| Accessible MFA disable flow | Modal-style confirmation replaces browser prompt/confirm behavior | [`frontend/src/pages/Profile.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Profile.jsx) |
| Progress and live status semantics | Resource indicators expose labels and live announcements | [`frontend/src/components/ResourceMonitor.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/ResourceMonitor.jsx) |
| White-screen resilience | Error boundary prevents a single route failure from blanking the entire app | [`frontend/src/components/ErrorBoundary.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/ErrorBoundary.jsx), [`frontend/src/App.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/App.jsx) |

### Authentication And Identity

| Area | Current Implementation | Evidence |
|---|---|---|
| Current user/profile alignment | Frontend auth requests and profile responses match active backend contracts | [`frontend/src/services/auth.js`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/services/auth.js), [`backend/auth-service/src/main/java/com/foodsec/auth/controller/AuthController.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/auth-service/src/main/java/com/foodsec/auth/controller/AuthController.java), [`backend/auth-service/src/main/java/com/foodsec/auth/service/AuthService.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/auth-service/src/main/java/com/foodsec/auth/service/AuthService.java) |
| Profile update support | Auth service supports profile updates through a dedicated DTO and service path | [`backend/auth-service/src/main/java/com/foodsec/auth/dto/UpdateProfileRequest.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/auth-service/src/main/java/com/foodsec/auth/dto/UpdateProfileRequest.java), [`backend/auth-service/src/main/java/com/foodsec/auth/controller/AuthController.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/auth-service/src/main/java/com/foodsec/auth/controller/AuthController.java) |
| MFA-oriented flow preservation | MFA remains part of the application while being made more accessible | [`frontend/src/pages/Profile.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Profile.jsx) |
| MBTI removed from all product layers | MBTI fields and personality-tailoring code removed from auth, AI model, LLM service, and database | `database/postgres/migrations/V2__remove_mbti_columns.sql`, `backend/llm-service`, `ai-model` |

### Secure Routing, Transport, And Boundary Controls

| Area | Current Implementation | Evidence |
|---|---|---|
| Gateway path rewrite alignment | API gateway routes map to real downstream Spring paths | [`backend/api-gateway/src/main/java/com/foodsec/gateway/config/GatewayConfig.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/api-gateway/src/main/java/com/foodsec/gateway/config/GatewayConfig.java) |
| Gateway security headers | Gateway emits baseline hardening headers for browser-facing responses | [`backend/api-gateway/src/main/java/com/foodsec/gateway/filter/SecurityHeadersFilter.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/api-gateway/src/main/java/com/foodsec/gateway/filter/SecurityHeadersFilter.java) |
| Auth service security headers | Auth service emits content-type, frame, referrer, HSTS, and permissions protections | [`backend/auth-service/src/main/java/com/foodsec/auth/config/SecurityConfig.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/auth-service/src/main/java/com/foodsec/auth/config/SecurityConfig.java) |
| Frontend gateway defaults | Frontend points to the API gateway rather than stale direct-service ports | [`frontend/.env.example`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/.env.example), [`frontend/src/services/api.js`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/services/api.js), [`frontend/src/services/websocket.js`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/services/websocket.js) |

### Collaboration And Real-Time Behavior

| Area | Current Implementation | Evidence |
|---|---|---|
| Real-time transport consistency | Frontend uses STOMP over SockJS rather than an incompatible socket.io client | [`frontend/src/services/websocket.js`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/services/websocket.js), [`frontend/package.json`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/package.json) |
| Session listing and history support | Collaboration UI can load real session metadata and prior activity | [`frontend/src/components/CollabPanel.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/CollabPanel.jsx), [`frontend/src/pages/Collaborate.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Collaborate.jsx), [`backend/collaboration-service/src/main/java/com/foodsecuritynet/collaboration/controller/WebSocketController.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/collaboration-service/src/main/java/com/foodsecuritynet/collaboration/controller/WebSocketController.java), [`backend/collaboration-service/src/main/java/com/foodsecuritynet/collaboration/service/CollaborationSessionService.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/collaboration-service/src/main/java/com/foodsecuritynet/collaboration/service/CollaborationSessionService.java) |

### Data Handling, Analysis, And Export

| Area | Current Implementation | Evidence |
|---|---|---|
| Active analysis contract alignment | Analyze and troubleshooting flows use current backend request and response shapes | [`frontend/src/pages/Analyze.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/pages/Analyze.jsx), [`frontend/src/components/LLMChat.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/LLMChat.jsx), [`backend/llm-service/src/main/java/com/foodsecuritynet/llm/controller/LlmQueryController.java`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/backend/llm-service/src/main/java/com/foodsecuritynet/llm/controller/LlmQueryController.java) |
| Export affordances | Export actions use understandable labels and corrected icon presentation | [`frontend/src/components/ExportTool.jsx`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/src/components/ExportTool.jsx) |
| Mock/demo support | Demo API supports local review without requiring full backend startup | [`demo-server.js`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/demo-server.js) |

### Documentation And Audit Friendliness

| Area | Current Implementation | Evidence |
|---|---|---|
| Root system description | README now reflects the live architecture and user-visible capabilities | [`README.md`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/README.md) |
| Frontend implementation notes | Frontend README reflects current stack, endpoints, and accessibility work | [`frontend/README.md`](/Users/sekac/Documents/My-Projects-LOCAL/FoodSecurityNet/frontend/README.md) |
| Stale tracker removal | Clearly outdated improvement/test summary files were removed to reduce review confusion | Commit/worktree changes in this repository, including deletion of `TEST_SUMMARY.md`, `IMPROVEMENTS.md`, and `tests/E2E_TEST_GUIDE.md` |

## Known Gaps

The repository is more reviewable than before, but these items still need attention for deeper readiness:

- Full manual keyboard and screen-reader audit across every route and modal
- Formal color-contrast validation across all interactive states
- Stronger form-level error association and recovery guidance in every workflow
- Automated accessibility testing coverage
- Broader authorization and access-control test coverage
- Dependency scanning, secret scanning, and supply-chain review automation
- Centralized audit logging and retention guidance across all services
- Content Security Policy design and rollout
- Backend compilation and test verification in an environment with Maven available

## Reviewer Notes

- This document is intended to reduce time spent reconciling code, docs, and live product behavior.
- Claims here are implementation-oriented and evidence-oriented, not certification claims.
- The safest review position is: the application is being prepared for external accessibility and security review while preserving core functionality.

## Suggested Next Implementation Pass

1. Add automated accessibility checks and route-level manual test notes.
2. Expand auth/session/access-control regression tests.
3. Add documented logging and monitoring expectations per service.
