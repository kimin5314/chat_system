#!/bin/bash

# Production deployment script for the chat system

echo "Building and deploying production environment..."

# Set production environment variables
export SPRING_PROFILES_ACTIVE=prod
export FRONTEND_URL="http://kimin.pink:5173"
export DB_USERNAME="kimin"
export DB_PASSWORD="kmn@68A1BH"
export UPLOAD_DIR="/home/kimin/chat_system/uploads"
export AVATAR_DIR="/home/kimin/chat_system/uploads/avatar"

# Build frontend
echo "Building frontend for production..."
cd front
npm run build

# Build backend
echo "Building backend for production..."
cd ../back
./mvnw clean package -DskipTests

echo "Production build complete!"
echo "Frontend build is in: front/dist/"
echo "Backend JAR is in: back/target/springboot-0.0.1-SNAPSHOT.jar"
echo ""
echo "To run production backend:"
echo "java -jar back/target/springboot-0.0.1-SNAPSHOT.jar"
