#!/bin/bash

# ===========================================
# ServiceDesk Platform - Production Build Script
# ===========================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build"
BACKEND_DIR="$PROJECT_ROOT/backend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}ServiceDesk Platform - Production Build${NC}"
echo -e "${GREEN}=========================================${NC}"

# Check Java version
echo -e "\n${YELLOW}Checking Java version...${NC}"
java -version 2>&1 | head -1
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Error: Java 17+ is required${NC}"
    exit 1
fi
echo -e "${GREEN}Java version OK${NC}"

# Check Maven
echo -e "\n${YELLOW}Checking Maven...${NC}"
mvn -version | head -1
echo -e "${GREEN}Maven OK${NC}"

# Create build directory
echo -e "\n${YELLOW}Creating build directory...${NC}"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/jars"
mkdir -p "$BUILD_DIR/docker"

# Build all services
echo -e "\n${YELLOW}Building all backend services...${NC}"
cd "$BACKEND_DIR"

# Clean and package
mvn clean package -DskipTests -q

# Copy JARs to build directory
echo -e "\n${YELLOW}Copying JAR files...${NC}"

SERVICES=(
    "ticket-service"
    "channel-service"
    "knowledge-service"
    "notification-service"
    "ai-service"
    "marketplace-service"
    "analytics-service"
    "api-gateway"
)

for SERVICE in "${SERVICES[@]}"; do
    if [ -d "$BACKEND_DIR/$SERVICE/target" ]; then
        JAR_FILE=$(ls "$BACKEND_DIR/$SERVICE/target"/*.jar 2>/dev/null | grep -v original | head -1)
        if [ -n "$JAR_FILE" ]; then
            cp "$JAR_FILE" "$BUILD_DIR/jars/${SERVICE}.jar"
            echo -e "  ${GREEN}✓${NC} $SERVICE.jar"
        else
            echo -e "  ${RED}✗${NC} $SERVICE - JAR not found"
        fi
    fi
done

# Copy Docker files
echo -e "\n${YELLOW}Copying Docker configuration...${NC}"
cp "$PROJECT_ROOT/docker-compose.yml" "$BUILD_DIR/docker/"
cp "$PROJECT_ROOT/docker-compose.dev.yml" "$BUILD_DIR/docker/"
cp "$PROJECT_ROOT/.env.example" "$BUILD_DIR/docker/"
cp -r "$PROJECT_ROOT/infrastructure" "$BUILD_DIR/docker/"
cp -r "$PROJECT_ROOT/scripts" "$BUILD_DIR/docker/"

# Create deployment README
cat > "$BUILD_DIR/DEPLOY.md" << 'EOF'
# ServiceDesk Platform - Deployment Guide

## JAR Files

All service JAR files are in the `jars/` directory:

| Service | JAR | Port |
|---------|-----|------|
| API Gateway | api-gateway.jar | 8080 |
| Ticket Service | ticket-service.jar | 8081 |
| Channel Service | channel-service.jar | 8082 |
| Knowledge Service | knowledge-service.jar | 8083 |
| Notification Service | notification-service.jar | 8084 |
| AI Service | ai-service.jar | 8085 |
| Marketplace Service | marketplace-service.jar | 8086 |
| Analytics Service | analytics-service.jar | 8087 |

## Option 1: Docker Deployment (Recommended)

```bash
cd docker/
cp .env.example .env
# Edit .env with your settings

docker-compose up -d
```

## Option 2: Manual JAR Deployment

### Prerequisites
- PostgreSQL 16
- Redis 7
- Elasticsearch 8.x
- RabbitMQ 3.x

### Run Services

```bash
# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=servicedesk
export DB_USERNAME=servicedesk
export DB_PASSWORD=your-password
export REDIS_HOST=localhost
export JWT_SECRET=your-secret-key-at-least-32-characters

# Run each service
java -jar jars/ticket-service.jar &
java -jar jars/channel-service.jar &
java -jar jars/knowledge-service.jar &
java -jar jars/notification-service.jar &
java -jar jars/ai-service.jar &
java -jar jars/marketplace-service.jar &
java -jar jars/analytics-service.jar &
java -jar jars/api-gateway.jar &
```

## Systemd Service (Linux)

Create `/etc/systemd/system/servicedesk-ticket.service`:

```ini
[Unit]
Description=ServiceDesk Ticket Service
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=servicedesk
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_HOST=localhost"
Environment="JWT_SECRET=your-secret"
ExecStart=/usr/bin/java -Xms512m -Xmx1g -jar /opt/servicedesk/ticket-service.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable servicedesk-ticket
sudo systemctl start servicedesk-ticket
```

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Access

- API: http://your-server:8080
- Swagger UI: http://your-server:8080/swagger-ui.html
EOF

# Print summary
echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}Build completed successfully!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "\nBuild output: $BUILD_DIR"
echo -e "\nContents:"
ls -la "$BUILD_DIR/jars/"

echo -e "\n${YELLOW}Next steps:${NC}"
echo "1. Copy $BUILD_DIR to your server"
echo "2. Follow instructions in $BUILD_DIR/DEPLOY.md"
