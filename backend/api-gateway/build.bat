@echo off
REM FoodSecurityNet API Gateway - Build Script for Windows

echo Building FoodSecurityNet API Gateway...

REM Clean and build
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo Build successful!
    echo JAR file: target\api-gateway-1.0.0.jar
) else (
    echo Build failed!
    exit /b 1
)

REM Build Docker image
set /p BUILD_DOCKER="Build Docker image? (y/n): "
if /i "%BUILD_DOCKER%"=="y" (
    docker build -t foodsec-api-gateway:latest .
    echo Docker image built: foodsec-api-gateway:latest
)
