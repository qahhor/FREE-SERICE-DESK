#!/bin/bash

###########################################
# ServiceDesk Monolith - Production Deployment Script
# Automates deployment to production environment
###########################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DEPLOY_METHOD=${DEPLOY_METHOD:-"docker"}  # docker, jar, or kubernetes
DEPLOY_DIR=${DEPLOY_DIR:-"/opt/servicedesk"}

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk Production Deployment${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Function: Display Usage
###########################################
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -m, --method METHOD    Deployment method: docker, jar, kubernetes (default: docker)"
    echo "  -d, --dir DIRECTORY    Deployment directory (default: /opt/servicedesk)"
    echo "  -h, --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                     # Deploy using Docker (default)"
    echo "  $0 -m jar              # Deploy as standalone JAR"
    echo "  $0 -m kubernetes       # Deploy to Kubernetes"
    echo "  $0 -d /custom/path     # Deploy to custom directory"
    echo ""
    exit 1
}

###########################################
# Parse Arguments
###########################################
while [[ $# -gt 0 ]]; do
    case $1 in
        -m|--method)
            DEPLOY_METHOD="$2"
            shift 2
            ;;
        -d|--dir)
            DEPLOY_DIR="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            usage
            ;;
    esac
done

# Validate deployment method
if [[ ! "$DEPLOY_METHOD" =~ ^(docker|jar|kubernetes)$ ]]; then
    echo -e "${RED}Invalid deployment method: $DEPLOY_METHOD${NC}"
    echo -e "${YELLOW}Valid methods: docker, jar, kubernetes${NC}"
    exit 1
fi

echo -e "Deployment Method: ${GREEN}$DEPLOY_METHOD${NC}"
echo -e "Deployment Directory: ${GREEN}$DEPLOY_DIR${NC}"
echo ""

###########################################
# Step 1: Pre-Deployment Checks
###########################################
echo -e "${YELLOW}[1/5] Running pre-deployment checks...${NC}"

# Check if running as root for system-wide deployment
if [[ "$DEPLOY_DIR" == /opt/* ]] || [[ "$DEPLOY_DIR" == /usr/* ]]; then
    if [[ $EUID -ne 0 ]]; then
        echo -e "${RED}ERROR: This script must be run as root for system-wide deployment${NC}"
        echo -e "${YELLOW}Try: sudo $0${NC}"
        exit 1
    fi
fi

# Check deployment method prerequisites
case $DEPLOY_METHOD in
    docker)
        if ! command -v docker &> /dev/null; then
            echo -e "${RED}ERROR: Docker is not installed${NC}"
            exit 1
        fi
        if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
            echo -e "${RED}ERROR: Docker Compose is not installed${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ Docker and Docker Compose found${NC}"
        ;;
    jar)
        if ! command -v java &> /dev/null; then
            echo -e "${RED}ERROR: Java is not installed${NC}"
            exit 1
        fi
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -lt 17 ]; then
            echo -e "${RED}ERROR: Java 17 or higher is required${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ Java $(java -version 2>&1 | head -1 | cut -d'"' -f2) found${NC}"
        ;;
    kubernetes)
        if ! command -v kubectl &> /dev/null; then
            echo -e "${RED}ERROR: kubectl is not installed${NC}"
            exit 1
        fi
        echo -e "${GREEN}✓ kubectl found${NC}"
        ;;
esac

# Check if build directory exists
if [ ! -d "$PROJECT_ROOT/build" ]; then
    echo -e "${YELLOW}WARNING: Build directory not found${NC}"
    echo -e "${YELLOW}Running build script first...${NC}"
    cd "$PROJECT_ROOT"
    ./scripts/build-production.sh
fi

echo ""

###########################################
# Step 2: Create Deployment Directory
###########################################
echo -e "${YELLOW}[2/5] Preparing deployment directory...${NC}"

mkdir -p "$DEPLOY_DIR"
cd "$DEPLOY_DIR"

echo -e "${GREEN}✓ Deployment directory ready${NC}"
echo ""

###########################################
# Step 3: Copy Artifacts
###########################################
echo -e "${YELLOW}[3/5] Copying artifacts...${NC}"

cp -r "$PROJECT_ROOT/build"/* "$DEPLOY_DIR/"

echo -e "${GREEN}✓ Artifacts copied${NC}"
echo ""

###########################################
# Step 4: Configure Environment
###########################################
echo -e "${YELLOW}[4/5] Configuring environment...${NC}"

case $DEPLOY_METHOD in
    docker)
        if [ ! -f "$DEPLOY_DIR/docker/.env" ]; then
            echo -e "${YELLOW}Creating .env file from template...${NC}"
            cp "$DEPLOY_DIR/docker/.env.example" "$DEPLOY_DIR/docker/.env"

            # Generate secure secrets
            JWT_SECRET=$(openssl rand -base64 48)
            DB_PASSWORD=$(openssl rand -base64 32)
            MINIO_SECRET=$(openssl rand -base64 32)
            GRAFANA_PASS=$(openssl rand -base64 24)

            # Update .env with generated secrets
            sed -i "s|JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" "$DEPLOY_DIR/docker/.env"
            sed -i "s|DB_PASSWORD=.*|DB_PASSWORD=${DB_PASSWORD}|" "$DEPLOY_DIR/docker/.env"
            sed -i "s|MINIO_SECRET_KEY=.*|MINIO_SECRET_KEY=${MINIO_SECRET}|" "$DEPLOY_DIR/docker/.env"
            sed -i "s|GRAFANA_ADMIN_PASSWORD=.*|GRAFANA_ADMIN_PASSWORD=${GRAFANA_PASS}|" "$DEPLOY_DIR/docker/.env"

            echo -e "${GREEN}✓ .env file created with secure secrets${NC}"
            echo -e "${YELLOW}⚠️  IMPORTANT: Review and update $DEPLOY_DIR/docker/.env with production values${NC}"
        else
            echo -e "${GREEN}✓ .env file already exists${NC}"
        fi
        ;;
    jar)
        # Create systemd service
        if [ ! -f "/etc/systemd/system/servicedesk.service" ]; then
            echo -e "${YELLOW}Creating systemd service...${NC}"

            # Create servicedesk user if doesn't exist
            if ! id "servicedesk" &>/dev/null; then
                useradd -r -s /bin/false -d "$DEPLOY_DIR" servicedesk
            fi

            # Create config and logs directories
            mkdir -p "$DEPLOY_DIR/config"
            mkdir -p "$DEPLOY_DIR/logs"
            mkdir -p /var/log/servicedesk

            # Copy application config
            cp "$DEPLOY_DIR/config/application-production.yml" "$DEPLOY_DIR/config/application.yml" 2>/dev/null || true

            # Create environment file
            cat > "$DEPLOY_DIR/config/servicedesk.env" <<EOF
DB_HOST=localhost
DB_PORT=5432
DB_NAME=servicedesk
DB_USERNAME=servicedesk
DB_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 48)
REDIS_HOST=localhost
REDIS_PORT=6379
ELASTICSEARCH_URIS=http://localhost:9200
EOF

            chmod 600 "$DEPLOY_DIR/config/servicedesk.env"

            # Create systemd service
            cat > /etc/systemd/system/servicedesk.service <<EOF
[Unit]
Description=ServiceDesk Monolithic Application
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=servicedesk
Group=servicedesk
WorkingDirectory=$DEPLOY_DIR
EnvironmentFile=$DEPLOY_DIR/config/servicedesk.env
ExecStart=/usr/bin/java -Xms1g -Xmx2g -jar $DEPLOY_DIR/artifacts/servicedesk-monolith-latest.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

            # Set ownership
            chown -R servicedesk:servicedesk "$DEPLOY_DIR"
            chown -R servicedesk:servicedesk /var/log/servicedesk

            # Reload systemd
            systemctl daemon-reload

            echo -e "${GREEN}✓ Systemd service created${NC}"
            echo -e "${YELLOW}⚠️  IMPORTANT: Update $DEPLOY_DIR/config/servicedesk.env with production values${NC}"
        else
            echo -e "${GREEN}✓ Systemd service already exists${NC}"
        fi
        ;;
    kubernetes)
        echo -e "${YELLOW}For Kubernetes deployment:${NC}"
        echo -e "  1. Build and push Docker image"
        echo -e "  2. Create namespace: kubectl create namespace servicedesk"
        echo -e "  3. Create secrets: See PRODUCTION-DEPLOYMENT.md"
        echo -e "  4. Apply manifests: kubectl apply -f infrastructure/kubernetes/"
        ;;
esac

echo ""

###########################################
# Step 5: Deploy Application
###########################################
echo -e "${YELLOW}[5/5] Deploying application...${NC}"

case $DEPLOY_METHOD in
    docker)
        cd "$DEPLOY_DIR/docker"

        # Check if services are already running
        if docker-compose -f docker-compose.monolith.yml ps | grep -q "Up"; then
            echo -e "${YELLOW}Stopping existing services...${NC}"
            docker-compose -f docker-compose.monolith.yml down
        fi

        echo -e "${YELLOW}Starting services...${NC}"
        docker-compose -f docker-compose.monolith.yml up -d

        echo -e "${YELLOW}Waiting for application to start...${NC}"
        sleep 10

        # Check health
        for i in {1..12}; do
            if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
                echo -e "${GREEN}✓ Application is healthy${NC}"
                break
            fi
            echo -n "."
            sleep 5
        done
        echo ""

        # Show logs
        echo -e "${YELLOW}Recent logs:${NC}"
        docker-compose -f docker-compose.monolith.yml logs --tail=20 servicedesk-monolith
        ;;
    jar)
        echo -e "${YELLOW}Starting servicedesk service...${NC}"
        systemctl enable servicedesk
        systemctl start servicedesk

        echo -e "${YELLOW}Waiting for application to start...${NC}"
        sleep 15

        # Check status
        if systemctl is-active --quiet servicedesk; then
            echo -e "${GREEN}✓ Service is running${NC}"
        else
            echo -e "${RED}✗ Service failed to start${NC}"
            echo -e "${YELLOW}Check logs: journalctl -u servicedesk -n 50${NC}"
            exit 1
        fi

        # Show recent logs
        echo -e "${YELLOW}Recent logs:${NC}"
        journalctl -u servicedesk -n 20 --no-pager
        ;;
    kubernetes)
        echo -e "${YELLOW}Kubernetes deployment requires manual steps${NC}"
        echo -e "See: $DEPLOY_DIR/docs/PRODUCTION-DEPLOYMENT.md"
        ;;
esac

echo ""

###########################################
# Deployment Summary
###########################################
echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}Deployment Summary${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo -e "Method:             ${GREEN}$DEPLOY_METHOD${NC}"
echo -e "Directory:          ${GREEN}$DEPLOY_DIR${NC}"
echo -e "Status:             ${GREEN}Deployed${NC}"
echo ""

case $DEPLOY_METHOD in
    docker)
        echo -e "${YELLOW}📋 Next Steps:${NC}"
        echo -e "  1. Update $DEPLOY_DIR/docker/.env with production values"
        echo -e "  2. Restart services: cd $DEPLOY_DIR/docker && docker-compose -f docker-compose.monolith.yml restart"
        echo -e "  3. Configure reverse proxy (nginx/Apache)"
        echo -e "  4. Set up SSL/TLS certificates"
        echo -e "  5. Configure backups"
        echo ""
        echo -e "${YELLOW}📊 Management Commands:${NC}"
        echo -e "  View logs:    cd $DEPLOY_DIR/docker && docker-compose -f docker-compose.monolith.yml logs -f"
        echo -e "  Stop:         cd $DEPLOY_DIR/docker && docker-compose -f docker-compose.monolith.yml stop"
        echo -e "  Start:        cd $DEPLOY_DIR/docker && docker-compose -f docker-compose.monolith.yml start"
        echo -e "  Restart:      cd $DEPLOY_DIR/docker && docker-compose -f docker-compose.monolith.yml restart"
        ;;
    jar)
        echo -e "${YELLOW}📋 Next Steps:${NC}"
        echo -e "  1. Update $DEPLOY_DIR/config/servicedesk.env with production values"
        echo -e "  2. Restart service: sudo systemctl restart servicedesk"
        echo -e "  3. Configure reverse proxy (nginx/Apache)"
        echo -e "  4. Set up SSL/TLS certificates"
        echo -e "  5. Configure backups"
        echo ""
        echo -e "${YELLOW}📊 Management Commands:${NC}"
        echo -e "  View logs:    sudo journalctl -u servicedesk -f"
        echo -e "  Status:       sudo systemctl status servicedesk"
        echo -e "  Stop:         sudo systemctl stop servicedesk"
        echo -e "  Start:        sudo systemctl start servicedesk"
        echo -e "  Restart:      sudo systemctl restart servicedesk"
        ;;
esac

echo ""
echo -e "${YELLOW}🔍 Health Check:${NC}"
echo -e "  curl http://localhost:8080/actuator/health"
echo ""
echo -e "${YELLOW}📚 Documentation:${NC}"
echo -e "  Full guide: $DEPLOY_DIR/docs/PRODUCTION-DEPLOYMENT.md"
echo -e "  Quick start: $DEPLOY_DIR/QUICK_START.md"
echo ""
echo -e "${GREEN}✓ Deployment completed successfully!${NC}"
echo -e "${GREEN}Happy deploying! 🎉${NC}"
echo ""
