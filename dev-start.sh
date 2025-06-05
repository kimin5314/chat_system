#!/bin/bash

# Development environment setup script for the chat system

echo "Setting up development environment..."

# Frontend development
echo "Starting frontend development server..."
cd front
npm run dev &
FRONTEND_PID=$!

# Backend development  
echo "Starting backend development server..."
cd ../back
export FRONTEND_URL="http://localhost:5173"
export DB_USERNAME="kimin"
export DB_PASSWORD="kmn@68A1BH"
export UPLOAD_DIR="/home/kimin/myproject/uploads"
export AVATAR_DIR="/home/kimin/myproject/uploads/avatar"

./mvnw spring-boot:run &
BACKEND_PID=$!

echo "Development servers started!"
echo "Frontend PID: $FRONTEND_PID"
echo "Backend PID: $BACKEND_PID"
echo "Frontend: http://localhost:5173"
echo "Backend: http://localhost:9090"
echo ""
echo "To stop servers, run: kill $FRONTEND_PID $BACKEND_PID"
