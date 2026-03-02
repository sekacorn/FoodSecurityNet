@echo off
REM FoodSecurityNet API Gateway - Run Script for Windows

echo Starting FoodSecurityNet API Gateway...

REM Check if Redis is running
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    echo Warning: Redis is not running. Please start Redis manually or with Docker:
    echo docker run -d -p 6379:6379 --name foodsec-redis redis:7-alpine
    echo.
    echo Continuing anyway...
)

REM Run the application
mvn spring-boot:run
