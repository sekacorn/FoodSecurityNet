#!/bin/bash

# FoodSecurityNet API Gateway - Build Script

echo "Building FoodSecurityNet API Gateway..."

# Clean and build
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "JAR file: target/api-gateway-1.0.0.jar"
else
    echo "Build failed!"
    exit 1
fi

# Build Docker image
read -p "Build Docker image? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker build -t foodsec-api-gateway:latest .
    echo "Docker image built: foodsec-api-gateway:latest"
fi
