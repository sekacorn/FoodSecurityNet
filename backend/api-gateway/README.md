# FoodSecurityNet API Gateway

The API gateway is the browser-facing entry point for FoodSecurityNet. It receives client traffic on port `8080`, applies gateway-level controls, and forwards requests to downstream services.

## Current Responsibilities

- route client API traffic to backend services
- validate JWTs and forward user context headers
- apply Redis-backed rate limiting
- expose health and Prometheus endpoints
- return fallback responses when downstream services are unavailable
- add baseline browser security headers

## Main Routes

| Incoming Path | Downstream Service |
|---|---|
| `/api/auth/**` | `auth-service` |
| `/api/agri-data/**` | `agri-integrator` |
| `/api/visualizations/**` | `agri-visualizer` |
| `/api/sessions/**` | `user-session` |
| `/api/llm/**` | `llm-service` |
| `/api/collaboration/**` | `collaboration-service` |
| `/ws/**` | collaboration WebSocket endpoint |

## Local Development

```bash
mvn clean install
mvn spring-boot:run
```

Gateway URL:

```text
http://localhost:8080
```

## Dependencies

- Java 17+
- Maven 3.6+
- Redis

## CORS Notes

Default local frontend origins include:

- `http://localhost:3000`
- `http://127.0.0.1:3000`

Additional origins can be supplied with `CORS_ALLOWED_ORIGINS`.

## Important Files

```text
backend/api-gateway/
|-- src/main/java/com/foodsec/gateway/config/GatewayConfig.java
|-- src/main/java/com/foodsec/gateway/config/SecurityConfig.java
|-- src/main/java/com/foodsec/gateway/filter/SecurityHeadersFilter.java
|-- src/main/resources/application.yml
|-- Dockerfile
`-- README.md
```

## Monitoring

- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`
- `GET /actuator/gateway/routes`

## Notes

- The frontend now targets the gateway on port `8080`.
- Collaboration traffic uses STOMP over SockJS through gateway-managed HTTP/WebSocket paths.
- Route rewrites in `GatewayConfig.java` are important because downstream services use their own context paths.

## License

Apache License 2.0. See the repository [LICENSE](../../LICENSE).
