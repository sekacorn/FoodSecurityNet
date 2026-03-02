# FoodSecurityNet API Gateway

The API Gateway serves as the single entry point for all client requests to the FoodSecurityNet platform. It provides routing, authentication, rate limiting, and circuit breaker patterns for all microservices.

## Features

- **Intelligent Routing**: Routes requests to appropriate microservices based on path patterns
- **JWT Authentication**: Validates JWT tokens and extracts user information
- **Rate Limiting**: Redis-based rate limiting with different limits for authenticated/anonymous users
- **Circuit Breakers**: Resilience4j circuit breakers prevent cascading failures
- **CORS Handling**: Configurable CORS policies for frontend integration
- **Request Logging**: Comprehensive logging of all requests and responses
- **Health Checks**: Actuator endpoints for monitoring and health checks
- **Metrics**: Prometheus metrics for monitoring and alerting

## Architecture

```
Client Request
     |
     v
API Gateway (Port 8080)
     |
     +---> JWT Validation
     +---> Rate Limiting
     +---> CORS Handling
     +---> Circuit Breaker
     |
     v
Route to Microservice
     |
     +---> auth-service:8081
     +---> agri-integrator:8082
     +---> agri-visualizer:8083
     +---> user-session:8084
     +---> llm-service:8085
     +---> collaboration-service:8086
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis 7.0+
- Docker (optional)

## Quick Start

### Local Development

1. **Install Dependencies**
   ```bash
   mvn clean install
   ```

2. **Configure Environment**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start Redis**
   ```bash
   docker run -d -p 6379:6379 redis:7-alpine
   ```

4. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

   The gateway will start on `http://localhost:8080`

### Docker Deployment

1. **Build Image**
   ```bash
   docker build -t foodsec-api-gateway .
   ```

2. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

## Configuration

### Route Configuration

Routes are configured in `GatewayConfig.java`:

| Path Pattern | Target Service | Port |
|-------------|----------------|------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/agri-data/**` | agri-integrator | 8082 |
| `/api/visualizations/**` | agri-visualizer | 8083 |
| `/api/sessions/**` | user-session | 8084 |
| `/api/llm/**` | llm-service | 8085 |
| `/api/collaboration/**` | collaboration-service | 8086 |

### Rate Limits

- **Authenticated Users**: 100 requests/minute
- **Anonymous Users**: 20 requests/minute
- **LLM Endpoints**: 10 requests/minute

### Circuit Breaker Settings

All services have circuit breakers configured with:
- Sliding window size: 10 requests
- Failure rate threshold: 50%
- Wait duration in open state: 10-30s (varies by service)

## API Endpoints

### Public Endpoints (No Authentication)

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Token refresh
- `POST /api/auth/forgot-password` - Password reset
- `GET /actuator/health` - Health check

### Protected Endpoints (Require JWT)

All other endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt-token>
```

## Health Checks

- **Health Endpoint**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`
- **Gateway Routes**: `GET /actuator/gateway/routes`

Example health check:
```bash
curl http://localhost:8080/actuator/health
```

## Request Headers

### Incoming (from client)

- `Authorization: Bearer <token>` - JWT authentication token
- `Content-Type: application/json` - Request content type

### Outgoing (to microservices)

The gateway adds the following headers to requests:

- `X-User-Id` - User ID from JWT
- `X-User-Email` - User email from JWT
- `X-User-Role` - User role from JWT

### Response Headers

- `X-RateLimit-Limit` - Rate limit for the user
- `X-RateLimit-Remaining` - Remaining requests in current window

## Monitoring

### Logs

Logs are written to:
- Console: `stdout`
- File: `logs/api-gateway.log`

Log rotation:
- Max file size: 10MB
- Max history: 30 days
- Total cap: 1GB

### Metrics

Access Prometheus metrics at:
```
http://localhost:8080/actuator/prometheus
```

Key metrics:
- `http_server_requests_seconds` - Request duration
- `resilience4j_circuitbreaker_state` - Circuit breaker states
- `redis_commands_total` - Redis operations

## Error Handling

### Circuit Breaker Fallbacks

When a service is unavailable, the gateway returns:

```json
{
  "status": "SERVICE_UNAVAILABLE",
  "message": "Service is temporarily unavailable",
  "timestamp": "2025-10-25T10:30:00",
  "suggestion": "Please try again later. If the issue persists, contact support."
}
```

### Rate Limit Exceeded

```json
{
  "error": "Rate limit exceeded",
  "limit": 100,
  "retryAfter": 60
}
```

## Security

### JWT Token Validation

The gateway validates JWT tokens using HMAC-SHA256:
- Secret key configured via `JWT_SECRET` environment variable
- Token must be at least 256 bits
- Tokens expire after 24 hours (configurable)

### CORS

CORS is configured to allow requests from:
- `http://localhost:3000` (default frontend)
- Additional origins via `CORS_ALLOWED_ORIGINS` environment variable

## Development

### Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/foodsec/gateway/
│   │   │   ├── ApiGatewayApp.java
│   │   │   ├── config/
│   │   │   │   ├── GatewayConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── RedisConfig.java
│   │   │   ├── filter/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── RateLimitingFilter.java
│   │   │   │   └── LoggingFilter.java
│   │   │   └── controller/
│   │   │       └── FallbackController.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       ├── logback-spring.xml
│   │       └── banner.txt
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

### Adding New Routes

1. Update `GatewayConfig.java` to add new route
2. Add circuit breaker configuration in `application.yml`
3. Add fallback handler in `FallbackController.java`
4. Update documentation

### Testing

Run tests:
```bash
mvn test
```

## Troubleshooting

### Redis Connection Issues

Check Redis is running:
```bash
redis-cli ping
# Should return: PONG
```

### Circuit Breaker Always Open

Check downstream service health:
```bash
curl http://localhost:8081/actuator/health
```

View circuit breaker status:
```bash
curl http://localhost:8080/actuator/health
```

### High Request Latency

Check logs for slow requests:
```bash
grep "Slow request detected" logs/api-gateway.log
```

## Environment Variables

See `.env.example` for all available configuration options.

## License

Copyright 2025 FoodSecurityNet. All rights reserved.
