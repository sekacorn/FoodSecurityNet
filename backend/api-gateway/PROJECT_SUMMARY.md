# FoodSecurityNet API Gateway - Project Summary

## Overview
Complete API Gateway service for the FoodSecurityNet platform, providing a unified entry point for all client requests with security, routing, rate limiting, and resilience features.

## Project Structure

```
backend/api-gateway/
├── src/
│   └── main/
│       ├── java/com/foodsec/gateway/
│       │   ├── ApiGatewayApp.java                    # Main Spring Boot application
│       │   ├── config/
│       │   │   ├── GatewayConfig.java               # Route configurations
│       │   │   ├── SecurityConfig.java              # Security setup
│       │   │   ├── CorsConfig.java                  # CORS policies
│       │   │   └── RedisConfig.java                 # Redis configuration
│       │   ├── filter/
│       │   │   ├── JwtAuthenticationFilter.java     # JWT validation
│       │   │   ├── RateLimitingFilter.java          # Rate limiting logic
│       │   │   └── LoggingFilter.java               # Request/response logging
│       │   ├── controller/
│       │   │   └── FallbackController.java          # Circuit breaker fallbacks
│       │   └── exception/
│       │       └── GlobalExceptionHandler.java      # Global error handling
│       └── resources/
│           ├── application.yml                       # Main configuration
│           ├── application-docker.yml                # Docker-specific config
│           ├── logback-spring.xml                    # Logging configuration
│           └── banner.txt                            # Custom banner
├── pom.xml                                          # Maven dependencies
├── Dockerfile                                       # Production container
├── docker-compose.yml                               # Local development setup
├── .dockerignore                                    # Docker build exclusions
├── .gitignore                                       # Git exclusions
├── .env.example                                     # Environment variables template
├── README.md                                        # Comprehensive documentation
├── run.sh / run.bat                                # Quick start scripts
└── build.sh / build.bat                            # Build scripts
```

## Key Features Implemented

### 1. Routing & Load Balancing
- **6 microservice routes** configured:
  - auth-service (8081) - `/api/auth/**`
  - agri-integrator (8082) - `/api/agri-data/**`
  - agri-visualizer (8083) - `/api/visualizations/**`
  - user-session (8084) - `/api/sessions/**`, `/api/preferences/**`
  - llm-service (8085) - `/api/llm/**`, `/api/insights/**`
  - collaboration-service (8086) - `/api/collaboration/**`, `/api/forums/**`, `/api/resources/**`

### 2. Security Features
- **JWT Authentication**: HMAC-SHA256 token validation
- **User Context**: Extracts and forwards user info to downstream services
- **Public Endpoints**: Login, register, password reset excluded from auth
- **Stateless**: No session state maintained in gateway

### 3. Rate Limiting
- **Redis-backed** token bucket algorithm
- **Tiered limits**:
  - Authenticated users: 100 req/min
  - Anonymous users: 20 req/min
  - LLM endpoints: 10 req/min (cost optimization)
- **Rate limit headers** in responses
- **Fail-open** strategy if Redis is unavailable

### 4. Circuit Breakers (Resilience4j)
- **Individual breakers** for each microservice
- **Configurable thresholds**:
  - Sliding window: 10 requests
  - Failure rate: 50%
  - Half-open timeout: 10-30s
- **Custom fallbacks** for graceful degradation
- **Health monitoring** via actuator

### 5. CORS Support
- **Configurable origins** via environment variables
- **Full method support**: GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Credential support** enabled
- **Exposed headers** for rate limiting and auth

### 6. Observability
- **Request/Response logging** with duration tracking
- **Slow request detection** (>3s warning)
- **Prometheus metrics** export
- **Health checks** with circuit breaker status
- **Structured logging** with logback

### 7. Production Features
- **Docker support** with multi-stage builds
- **Health checks** for container orchestration
- **JVM optimization** with G1GC
- **Non-root user** in containers
- **Log rotation** (10MB files, 30 day retention)
- **Graceful shutdown** handling

## Configuration Files

### pom.xml
Dependencies:
- Spring Cloud Gateway 2023.0.0
- Spring Security 3.2.0
- JJWT 0.12.3
- Redis Reactive
- Resilience4j
- Actuator + Prometheus

### application.yml
Key configurations:
- Server port: 8080
- Redis connection settings
- Route definitions
- Circuit breaker thresholds
- CORS policies
- Rate limiting rules
- Actuator endpoints
- Logging levels

### Dockerfile
- Multi-stage build (Maven + JRE)
- Eclipse Temurin 17 Alpine
- Security: non-root user
- Health checks included
- JVM tuning: 512MB-1GB heap

## Environment Variables

Critical environment variables:
- `JWT_SECRET` - HMAC secret for JWT validation (min 256 bits)
- `REDIS_HOST` / `REDIS_PORT` - Redis connection
- `*_SERVICE_URL` - Microservice endpoints (6 services)
- `CORS_ALLOWED_ORIGINS` - Frontend URLs
- `SPRING_PROFILES_ACTIVE` - Active profile (dev/docker/prod)

## Running the Service

### Prerequisites
1. Java 17+
2. Maven 3.6+
3. Redis 7.0+

### Quick Start
```bash
# Linux/Mac
./run.sh

# Windows
run.bat

# Docker
docker-compose up -d
```

### Building
```bash
# Linux/Mac
./build.sh

# Windows
build.bat

# Maven only
mvn clean package -DskipTests
```

## API Examples

### Authentication Required
```bash
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/agri-data/weather
```

### Public Endpoint
```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com","password":"pass123"}'
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

## Monitoring Endpoints

- Health: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Prometheus: `GET /actuator/prometheus`
- Routes: `GET /actuator/gateway/routes`
- Info: `GET /actuator/info`

## Error Responses

### Unauthorized (401)
```json
{
  "error": "Invalid or expired JWT token",
  "status": 401
}
```

### Rate Limited (429)
```json
{
  "error": "Rate limit exceeded",
  "limit": 100,
  "retryAfter": 60
}
```

### Service Unavailable (503)
```json
{
  "status": "SERVICE_UNAVAILABLE",
  "message": "Authentication service is temporarily unavailable",
  "timestamp": "2025-10-25T10:30:00",
  "suggestion": "Please try again later. If the issue persists, contact support."
}
```

## Performance Considerations

1. **Async/Reactive**: All filters use reactive programming for non-blocking I/O
2. **Redis Caching**: Rate limit data cached in Redis for fast access
3. **Circuit Breakers**: Prevent cascading failures and reduce load
4. **Connection Pooling**: Redis connection pool configured (max 8 active)
5. **JVM Tuning**: G1GC with 200ms max pause time

## Security Best Practices

1. **JWT Secret**: Must be at least 256 bits, stored in environment variable
2. **HTTPS**: Use HTTPS in production (configure reverse proxy)
3. **CORS**: Restrict allowed origins to known frontends only
4. **Rate Limiting**: Prevent DoS attacks
5. **Non-root User**: Container runs as non-privileged user
6. **Input Validation**: All inputs validated before processing

## Future Enhancements

1. OAuth2/OIDC integration
2. API key authentication for third-party integrations
3. Request/response transformation
4. GraphQL gateway support
5. WebSocket routing
6. Advanced caching strategies
7. Distributed tracing (Zipkin/Jaeger)
8. API versioning support

## Testing

### Manual Testing
```bash
# Start services
docker-compose up -d

# Test health
curl http://localhost:8080/actuator/health

# Test rate limiting (repeat 101 times)
for i in {1..101}; do
  curl http://localhost:8080/api/auth/login
done
```

### Integration Testing
Run integration tests:
```bash
mvn verify
```

## Troubleshooting

### Issue: Redis Connection Failed
**Solution**: Ensure Redis is running on port 6379
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

### Issue: JWT Validation Failed
**Solution**: Check JWT_SECRET matches auth-service secret

### Issue: Circuit Breaker Always Open
**Solution**: Check downstream service health and logs

### Issue: CORS Errors
**Solution**: Add frontend URL to CORS_ALLOWED_ORIGINS

## Dependencies

### Main Dependencies
- spring-cloud-starter-gateway: 2023.0.0
- spring-boot-starter-security: 3.2.0
- jjwt-api: 0.12.3
- spring-boot-starter-data-redis-reactive: 3.2.0
- resilience4j: 2.1.0

### Build Tools
- Maven: 3.9+
- Java: 17+

## Contributing

When modifying the gateway:
1. Update route configurations in GatewayConfig.java
2. Add circuit breaker config in application.yml
3. Update fallback handlers if needed
4. Test all routes and security filters
5. Update documentation

## License

Copyright 2025 FoodSecurityNet. All rights reserved.
