# FoodSecurityNet API Gateway - Project Summary

## Overview

The API Gateway is the unified entry point for frontend and client traffic in FoodSecurityNet. It handles authentication, routing, rate limiting, CORS, resilience, and monitoring before forwarding requests to downstream services.

This document is intentionally high level. Use [README.md](README.md) for local setup, endpoint details, environment variables, testing steps, and troubleshooting.

## What This Service Owns

- Receives client traffic on port `8080`
- Validates JWTs and forwards user context headers
- Applies Redis-backed rate limits
- Routes requests to backend microservices
- Exposes actuator and Prometheus endpoints
- Returns circuit-breaker fallbacks when downstream services are degraded

## Downstream Services

| Service | Default Port | Example Routes |
|---------|--------------|----------------|
| `auth-service` | 8081 | `/api/auth/**` |
| `agri-integrator` | 8082 | `/api/agri-data/**` |
| `agri-visualizer` | 8083 | `/api/visualizations/**` |
| `user-session` | 8084 | `/api/sessions/**` |
| `llm-service` | 8085 | `/api/llm/**` |
| `collaboration-service` | 8086 | `/api/collaboration/**` |

## Important Files

- `src/main/java/com/foodsec/gateway/config/` for routes, security, CORS, and Redis wiring
- `src/main/java/com/foodsec/gateway/filter/` for JWT, rate limiting, and request logging
- `src/main/java/com/foodsec/gateway/controller/FallbackController.java` for resilience fallbacks
- `src/main/resources/application.yml` for service configuration
- `.env.example` for local environment variables

## Quick Links

- Local setup and Docker usage: [README.md](README.md#quick-start)
- Route documentation: [README.md](README.md#configuration)
- Monitoring endpoints: [README.md](README.md#health-checks)
- Troubleshooting: [README.md](README.md#troubleshooting)

## Example Fallback Response

```json
{
  "status": "SERVICE_UNAVAILABLE",
  "message": "Service is temporarily unavailable",
  "timestamp": "<ISO 8601 datetime>",
  "suggestion": "Please try again later. If the issue persists, contact support."
}
```

## License

Apache License 2.0 - See the repository [LICENSE](../../LICENSE).
