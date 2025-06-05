# Configuration Management

This project now uses proper configuration files instead of hardcoded URLs.

## Environment Variables

### Backend (Spring Boot)
- `FRONTEND_URL` - Frontend application URL (default: http://localhost:5173)
- `DB_USERNAME` - Database username (default: kimin)
- `DB_PASSWORD` - Database password (default: kmn@68A1BH)
- `UPLOAD_DIR` - File upload directory (default: /home/kimin/myproject/uploads)
- `AVATAR_DIR` - Avatar upload directory (default: /home/kimin/myproject/uploads/avatar)
- `SPRING_PROFILES_ACTIVE` - Spring profile (dev/prod)

### Frontend (Vite)
- `VITE_API_BASE` - Backend API base URL
- `VITE_WS_URL` - WebSocket URL for friend requests
- `VITE_WS_BASE` - WebSocket base URL

## Environment Files

### Frontend
- `.env` - Default configuration (development)
- `.env.development` - Development specific configuration
- `.env.production` - Production specific configuration

### Backend
- `application.yml` - Default configuration with environment variable placeholders
- `application-prod.yml` - Production specific configuration

## Development Setup

1. Use development environment:
   ```bash
   ./dev-start.sh
   ```

2. Or manually:
   ```bash
   # Frontend
   cd front && npm run dev
   
   # Backend  
   cd back && ./mvnw spring-boot:run
   ```

## Production Deployment

1. Build for production:
   ```bash
   ./prod-build.sh
   ```

2. Run production backend:
   ```bash
   java -jar back/target/springboot-0.0.1-SNAPSHOT.jar
   ```

## Configuration Benefits

- ✅ No more hardcoded URLs
- ✅ Easy environment switching
- ✅ Secure credential management through environment variables
- ✅ Production-ready configuration
- ✅ CORS properly configured per environment
- ✅ WebSocket origins properly restricted
