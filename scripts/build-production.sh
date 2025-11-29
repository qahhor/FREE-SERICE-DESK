#!/bin/bash

###########################################
# ServiceDesk Monolith - Production Build Script
# Build production-ready JAR artifact
###########################################

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$PROJECT_ROOT/build"
BACKEND_DIR="$PROJECT_ROOT/backend"
VERSION=${VERSION:-"1.0.0"}
BUILD_DATE=$(date +%Y%m%d_%H%M%S)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk Monolith - Production Build${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Step 1: Check Prerequisites
###########################################
echo -e "${YELLOW}[1/6] Checking prerequisites...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java is not installed${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}ERROR: Maven is not installed${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}ERROR: Java 17 or higher is required (found: Java $JAVA_VERSION)${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Java $(java -version 2>&1 | head -1 | cut -d'"' -f2) found${NC}"
echo -e "${GREEN}✓ Maven $(mvn --version | head -1 | awk '{print $3}') found${NC}"
echo ""

###########################################
# Step 2: Clean Previous Builds
###########################################
echo -e "${YELLOW}[2/6] Cleaning previous builds...${NC}"
cd "$BACKEND_DIR"
mvn clean -pl monolith-app -q
echo -e "${GREEN}✓ Clean completed${NC}"
echo ""

###########################################
# Step 3: Run Tests (optional)
###########################################
if [ "${SKIP_TESTS}" != "true" ]; then
    echo -e "${YELLOW}[3/6] Running tests...${NC}"
    mvn test -pl monolith-app
    echo -e "${GREEN}✓ All tests passed${NC}"
else
    echo -e "${YELLOW}[3/6] Skipping tests (SKIP_TESTS=true)${NC}"
fi
echo ""

###########################################
# Step 4: Build Production JAR
###########################################
echo -e "${YELLOW}[4/6] Building production JAR...${NC}"
mvn package -pl monolith-app -am -DskipTests \
    -Dspring.profiles.active=production \
    -Dmaven.test.skip=true

if [ ! -f "$BACKEND_DIR/monolith-app/target/servicedesk-monolith.jar" ]; then
    echo -e "${RED}ERROR: JAR file not found after build${NC}"
    exit 1
fi

echo -e "${GREEN}✓ JAR built successfully${NC}"
echo ""

###########################################
# Step 5: Package Artifacts
###########################################
echo -e "${YELLOW}[5/6] Packaging artifacts...${NC}"

# Create build directory structure
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/artifacts"
mkdir -p "$BUILD_DIR/config"
mkdir -p "$BUILD_DIR/docker"
mkdir -p "$BUILD_DIR/scripts"
mkdir -p "$BUILD_DIR/docs"

# Copy JAR
cp "$BACKEND_DIR/monolith-app/target/servicedesk-monolith.jar" \
   "$BUILD_DIR/artifacts/servicedesk-monolith-${VERSION}.jar"

# Create symlink to latest
cd "$BUILD_DIR/artifacts"
ln -sf "servicedesk-monolith-${VERSION}.jar" "servicedesk-monolith-latest.jar"
cd - > /dev/null

# Copy configuration templates
cp "$BACKEND_DIR/monolith-app/src/main/resources/application.yml" \
   "$BUILD_DIR/config/application.yml"

if [ -f "$BACKEND_DIR/monolith-app/src/main/resources/application-production.yml" ]; then
    cp "$BACKEND_DIR/monolith-app/src/main/resources/application-production.yml" \
       "$BUILD_DIR/config/application-production.yml"
fi

if [ -f "$BACKEND_DIR/monolith-app/src/main/resources/application-docker.yml" ]; then
    cp "$BACKEND_DIR/monolith-app/src/main/resources/application-docker.yml" \
       "$BUILD_DIR/config/application-docker.yml"
fi

# Copy Docker files
if [ -f "$BACKEND_DIR/monolith-app/Dockerfile" ]; then
    cp "$BACKEND_DIR/monolith-app/Dockerfile" "$BUILD_DIR/docker/"
fi

if [ -f "$PROJECT_ROOT/docker-compose.monolith.yml" ]; then
    cp "$PROJECT_ROOT/docker-compose.monolith.yml" "$BUILD_DIR/docker/"
fi

if [ -f "$PROJECT_ROOT/.env.example" ]; then
    cp "$PROJECT_ROOT/.env.example" "$BUILD_DIR/docker/.env.example"
fi

# Copy deployment scripts
if [ -f "$SCRIPT_DIR/deploy-production.sh" ]; then
    cp "$SCRIPT_DIR/deploy-production.sh" "$BUILD_DIR/scripts/"
fi

if [ -f "$SCRIPT_DIR/start-service.sh" ]; then
    cp "$SCRIPT_DIR/start-service.sh" "$BUILD_DIR/scripts/"
fi

# Copy documentation
if [ -f "$PROJECT_ROOT/DEPLOYMENT.md" ]; then
    cp "$PROJECT_ROOT/DEPLOYMENT.md" "$BUILD_DIR/docs/"
fi

if [ -f "$PROJECT_ROOT/PRODUCTION-DEPLOYMENT.md" ]; then
    cp "$PROJECT_ROOT/PRODUCTION-DEPLOYMENT.md" "$BUILD_DIR/docs/"
fi

if [ -f "$PROJECT_ROOT/README.md" ]; then
    cp "$PROJECT_ROOT/README.md" "$BUILD_DIR/docs/"
fi

echo -e "${GREEN}✓ Artifacts packaged${NC}"
echo ""

###########################################
# Step 6: Generate Build Information
###########################################
echo -e "${YELLOW}[6/6] Generating build information...${NC}"

# Get JAR file info
JAR_SIZE=$(du -h "$BUILD_DIR/artifacts/servicedesk-monolith-${VERSION}.jar" | cut -f1)
JAR_MD5=$(md5sum "$BUILD_DIR/artifacts/servicedesk-monolith-${VERSION}.jar" | cut -d' ' -f1)
JAR_SHA256=$(sha256sum "$BUILD_DIR/artifacts/servicedesk-monolith-${VERSION}.jar" | cut -d' ' -f1)

# Generate BUILD_INFO.txt
cat > "$BUILD_DIR/BUILD_INFO.txt" <<EOF
╔════════════════════════════════════════════════════════════╗
║     ServiceDesk Monolith - Production Build Information    ║
╚════════════════════════════════════════════════════════════╝

Build Information:
──────────────────
Version:        ${VERSION}
Build Date:     ${BUILD_DATE}
Built By:       $(whoami)
Build Host:     $(hostname)
Java Version:   $(java -version 2>&1 | head -1 | cut -d'"' -f2)
Maven Version:  $(mvn --version | head -1 | awk '{print $3}')

Artifacts:
──────────
Main JAR:       artifacts/servicedesk-monolith-${VERSION}.jar
Symlink:        artifacts/servicedesk-monolith-latest.jar
JAR Size:       ${JAR_SIZE}

Configuration Files:
────────────────────
- config/application.yml
- config/application-production.yml (if exists)
- config/application-docker.yml (if exists)

Docker Files:
─────────────
- docker/Dockerfile
- docker/docker-compose.monolith.yml
- docker/.env.example

Checksums:
──────────
MD5:            ${JAR_MD5}
SHA256:         ${JAR_SHA256}

Git Information:
────────────────
Branch:         $(git branch --show-current 2>/dev/null || echo "N/A")
Commit:         $(git rev-parse HEAD 2>/dev/null || echo "N/A")
Commit Message: $(git log -1 --pretty=%B 2>/dev/null | head -1 || echo "N/A")
Commit Date:    $(git log -1 --format=%cd 2>/dev/null || echo "N/A")

Quick Start:
────────────
1. Standalone JAR:
   java -jar artifacts/servicedesk-monolith-${VERSION}.jar

2. With external config:
   java -jar artifacts/servicedesk-monolith-${VERSION}.jar \\
     --spring.config.location=file:./config/application-production.yml

3. Docker Compose:
   cd docker/
   cp .env.example .env
   # Edit .env with production values
   docker-compose -f docker-compose.monolith.yml up -d

For detailed deployment instructions, see:
- docs/DEPLOYMENT.md
- docs/PRODUCTION-DEPLOYMENT.md (if exists)
- QUICK_START.md
EOF

# Create QUICK_START.md
cat > "$BUILD_DIR/QUICK_START.md" <<'EOF'
# ServiceDesk Monolith - Quick Start Guide

## 📦 What's in this build?

```
build/
├── artifacts/
│   ├── servicedesk-monolith-1.0.0.jar    # Main application JAR
│   └── servicedesk-monolith-latest.jar   # Symlink to latest
├── config/
│   ├── application.yml                    # Default configuration
│   ├── application-production.yml         # Production profile
│   └── application-docker.yml             # Docker profile
├── docker/
│   ├── Dockerfile                         # Container image definition
│   ├── docker-compose.monolith.yml        # Docker Compose setup
│   └── .env.example                       # Environment variables template
├── scripts/
│   └── deploy-production.sh               # Deployment helper script
├── docs/
│   ├── README.md                          # Project overview
│   ├── DEPLOYMENT.md                      # Detailed deployment guide
│   └── PRODUCTION-DEPLOYMENT.md           # Production-specific guide
├── BUILD_INFO.txt                         # Build metadata
└── QUICK_START.md                         # This file
```

## 🚀 Option 1: Run JAR Directly (Development/Testing)

### Prerequisites
- Java 17+
- PostgreSQL 16
- Redis 7
- Elasticsearch 8 (optional)

### Steps

```bash
# 1. Set required environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=servicedesk
export DB_USERNAME=servicedesk
export DB_PASSWORD=your_secure_password
export JWT_SECRET=$(openssl rand -base64 48)
export REDIS_HOST=localhost
export ELASTICSEARCH_URIS=http://localhost:9200

# 2. Run the application
java -Xms1g -Xmx2g -jar artifacts/servicedesk-monolith-latest.jar

# 3. Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Health: http://localhost:8080/actuator/health
```

## 🐳 Option 2: Docker Compose (Recommended for Production)

### Steps

```bash
# 1. Navigate to docker directory
cd docker/

# 2. Create environment file
cp .env.example .env

# 3. Edit .env with production values
nano .env

# IMPORTANT: Set these values:
# - JWT_SECRET (generate with: openssl rand -base64 48)
# - DB_PASSWORD
# - MAIL_* settings
# - Optional: AI API keys

# 4. Start all services
docker-compose -f docker-compose.monolith.yml up -d

# 5. Check status
docker-compose -f docker-compose.monolith.yml ps

# 6. View logs
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith

# 7. Verify health
curl http://localhost:8080/actuator/health
```

## 🔧 Option 3: Systemd Service (Linux Servers)

### Steps

```bash
# 1. Create service user
sudo useradd -r -s /bin/false servicedesk

# 2. Copy JAR to installation directory
sudo mkdir -p /opt/servicedesk
sudo cp artifacts/servicedesk-monolith-latest.jar /opt/servicedesk/
sudo chown -R servicedesk:servicedesk /opt/servicedesk

# 3. Create configuration
sudo mkdir -p /etc/servicedesk
sudo cp config/application-production.yml /etc/servicedesk/application.yml

# 4. Create environment file
sudo tee /etc/servicedesk/servicedesk.env > /dev/null <<ENVEOF
DB_HOST=localhost
DB_PORT=5432
DB_NAME=servicedesk
DB_USERNAME=servicedesk
DB_PASSWORD=your_secure_password
JWT_SECRET=$(openssl rand -base64 48)
REDIS_HOST=localhost
ELASTICSEARCH_URIS=http://localhost:9200
ENVEOF

sudo chmod 600 /etc/servicedesk/servicedesk.env
sudo chown servicedesk:servicedesk /etc/servicedesk/servicedesk.env

# 5. Create systemd service
sudo tee /etc/systemd/system/servicedesk.service > /dev/null <<SERVICEEOF
[Unit]
Description=ServiceDesk Monolithic Application
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=servicedesk
Group=servicedesk
WorkingDirectory=/opt/servicedesk
EnvironmentFile=/etc/servicedesk/servicedesk.env
ExecStart=/usr/bin/java -Xms1g -Xmx2g -jar /opt/servicedesk/servicedesk-monolith-latest.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
SERVICEEOF

# 6. Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable servicedesk
sudo systemctl start servicedesk

# 7. Check status
sudo systemctl status servicedesk

# 8. View logs
sudo journalctl -u servicedesk -f
```

## 🔐 Security Checklist

Before deploying to production:

- [ ] Generate strong JWT_SECRET (min 32 characters)
- [ ] Set strong database password
- [ ] Configure HTTPS/TLS (use reverse proxy like nginx)
- [ ] Set up firewall rules
- [ ] Enable and configure backup
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Review and update application-production.yml
- [ ] Disable unnecessary endpoints in production
- [ ] Set up log rotation
- [ ] Configure rate limiting

## 📊 Monitoring

Health check endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Metrics (Prometheus format):
```bash
curl http://localhost:8080/actuator/prometheus
```

Application info:
```bash
curl http://localhost:8080/actuator/info
```

## 🆘 Troubleshooting

### Application won't start
```bash
# Check logs
docker-compose -f docker/docker-compose.monolith.yml logs servicedesk-monolith

# Or for systemd
sudo journalctl -u servicedesk -n 100
```

### Database connection failed
- Verify PostgreSQL is running
- Check DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
- Test connection: `psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME`

### Out of memory
- Increase JVM heap: `-Xms2g -Xmx4g`
- Or in Docker Compose, set memory limits

## 📚 Additional Resources

- Full deployment guide: `docs/DEPLOYMENT.md`
- Production deployment: `docs/PRODUCTION-DEPLOYMENT.md`
- Project README: `docs/README.md`
- Build information: `BUILD_INFO.txt`

## 🎯 Default Credentials (Change in Production!)

- Admin: admin@servicedesk.local / admin123

**⚠️ IMPORTANT: Change default credentials immediately after first login!**

---

For support: support@greenwhite.uz
EOF

echo -e "${GREEN}✓ Build information generated${NC}"
echo ""

###########################################
# Build Summary
###########################################
echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}Build Summary${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo -e "Build Directory:    ${GREEN}$BUILD_DIR${NC}"
echo -e "JAR File:           ${GREEN}artifacts/servicedesk-monolith-${VERSION}.jar${NC}"
echo -e "JAR Size:           ${GREEN}${JAR_SIZE}${NC}"
echo -e "Build Info:         ${GREEN}BUILD_INFO.txt${NC}"
echo -e "Quick Start:        ${GREEN}QUICK_START.md${NC}"
echo ""
echo -e "${GREEN}✓ Production build completed successfully!${NC}"
echo ""
echo -e "${YELLOW}📋 Build Contents:${NC}"
cd "$BUILD_DIR"
tree -L 2 -I 'node_modules' 2>/dev/null || find . -maxdepth 2 -type d
cd - > /dev/null
echo ""
echo -e "${YELLOW}🚀 Next Steps:${NC}"
echo -e "  1. Review:    ${BLUE}$BUILD_DIR/BUILD_INFO.txt${NC}"
echo -e "  2. Test:      ${BLUE}java -jar $BUILD_DIR/artifacts/servicedesk-monolith-${VERSION}.jar${NC}"
echo -e "  3. Deploy:    ${BLUE}See $BUILD_DIR/QUICK_START.md${NC}"
echo -e "  4. Monitor:   ${BLUE}curl http://localhost:8080/actuator/health${NC}"
echo ""
echo -e "${GREEN}Happy deploying! 🎉${NC}"
echo ""
