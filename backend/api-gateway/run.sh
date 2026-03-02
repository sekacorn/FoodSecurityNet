#!/bin/bash

# FoodSecurityNet API Gateway - Run Script

echo "Starting FoodSecurityNet API Gateway..."

# Check if Redis is running
if ! redis-cli ping &> /dev/null; then
    echo "Warning: Redis is not running. Starting Redis with Docker..."
    docker run -d -p 6379:6379 --name foodsec-redis redis:7-alpine
    sleep 2
fi

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Run the application
mvn spring-boot:run
