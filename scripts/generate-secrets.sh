#!/bin/bash

###########################################
# ServiceDesk Platform - Secrets Generator
# Generates secure random secrets for all components
###########################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV_FILE="$PROJECT_ROOT/.env"
ENV_EXAMPLE="$PROJECT_ROOT/.env.production.example"

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk - Secrets Generator${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Check if .env exists
###########################################
if [ -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}WARNING: .env file already exists${NC}"
    read -p "Do you want to overwrite it? (yes/no): " CONFIRM
    if [ "$CONFIRM" != "yes" ]; then
        echo -e "${YELLOW}Creating .env.new instead${NC}"
        ENV_FILE="$PROJECT_ROOT/.env.new"
    fi
fi

###########################################
# Generate secrets
###########################################
echo -e "${YELLOW}Generating secure secrets...${NC}"
echo ""

# JWT Secret (64 characters for HS512)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
echo -e "${GREEN}✓ JWT_SECRET generated${NC}"

# Database password
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | tr -d '/' | tr -d '+')
echo -e "${GREEN}✓ DB_PASSWORD generated${NC}"

# Redis password
REDIS_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | tr -d '/' | tr -d '+')
echo -e "${GREEN}✓ REDIS_PASSWORD generated${NC}"

# MinIO password
MINIO_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | tr -d '/' | tr -d '+')
echo -e "${GREEN}✓ MINIO_PASSWORD generated${NC}"

# Grafana password
GRAFANA_PASSWORD=$(openssl rand -base64 16 | tr -d '\n' | tr -d '/' | tr -d '+')
echo -e "${GREEN}✓ GRAFANA_ADMIN_PASSWORD generated${NC}"

# Elasticsearch password
ELASTICSEARCH_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | tr -d '/' | tr -d '+')
echo -e "${GREEN}✓ ELASTICSEARCH_PASSWORD generated${NC}"

echo ""

###########################################
# Create .env file
###########################################
echo -e "${YELLOW}Creating .env file...${NC}"

cat > "$ENV_FILE" <<EOF
# =================================================================
# ServiceDesk Platform - Environment Configuration
# Generated: $(date '+%Y-%m-%d %H:%M:%S')
# =================================================================
# SECURITY WARNING: Keep this file secure and never commit to git!
# =================================================================

# =================================================================
# APPLICATION SETTINGS
# =================================================================
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080

# =================================================================
# SECURITY (Generated - DO NOT SHARE!)
# =================================================================
JWT_SECRET=${JWT_SECRET}

# =================================================================
# DATABASE
# =================================================================
DB_HOST=postgres
DB_PORT=5432
DB_NAME=servicedesk
DB_USERNAME=servicedesk
DB_PASSWORD=${DB_PASSWORD}

# =================================================================
# REDIS
# =================================================================
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD}

# =================================================================
# ELASTICSEARCH
# =================================================================
ELASTICSEARCH_URIS=http://elasticsearch:9200
# ELASTICSEARCH_USERNAME=elastic
# ELASTICSEARCH_PASSWORD=${ELASTICSEARCH_PASSWORD}

# =================================================================
# FILE STORAGE (MinIO)
# =================================================================
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=servicedesk
MINIO_SECRET_KEY=${MINIO_PASSWORD}
MINIO_BUCKET=servicedesk-files

# =================================================================
# EMAIL CONFIGURATION (Update these values!)
# =================================================================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@yourdomain.com
MAIL_FROM_NAME=ServiceDesk Platform

# =================================================================
# MONITORING
# =================================================================
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=${GRAFANA_PASSWORD}

# =================================================================
# SSL/DOMAIN (Update these values!)
# =================================================================
DOMAIN=servicedesk.yourdomain.com
SSL_EMAIL=admin@yourdomain.com

# =================================================================
# AI SERVICES (Optional)
# =================================================================
OPENAI_ENABLED=false
# OPENAI_API_KEY=sk-your-openai-api-key

ANTHROPIC_ENABLED=false
# ANTHROPIC_API_KEY=sk-ant-your-anthropic-api-key

# =================================================================
# INTEGRATIONS (Optional)
# =================================================================
TELEGRAM_ENABLED=false
# TELEGRAM_BOT_TOKEN=your-telegram-bot-token

WHATSAPP_ENABLED=false
# WHATSAPP_ACCESS_TOKEN=your-whatsapp-token

# =================================================================
# LOGGING
# =================================================================
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_SERVICEDESK=INFO
EOF

chmod 600 "$ENV_FILE"

echo -e "${GREEN}✓ .env file created: $ENV_FILE${NC}"
echo ""

###########################################
# Summary
###########################################
echo -e "${BLUE}=====================================${NC}"
echo -e "${GREEN}Secrets Generated Successfully!${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo -e "${YELLOW}Generated secrets:${NC}"
echo -e "  JWT_SECRET:              ${GREEN}[64 characters]${NC}"
echo -e "  DB_PASSWORD:             ${GREEN}[32 characters]${NC}"
echo -e "  REDIS_PASSWORD:          ${GREEN}[32 characters]${NC}"
echo -e "  MINIO_PASSWORD:          ${GREEN}[32 characters]${NC}"
echo -e "  GRAFANA_ADMIN_PASSWORD:  ${GREEN}[22 characters]${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Edit ${BLUE}$ENV_FILE${NC}"
echo -e "  2. Update EMAIL configuration (MAIL_*)"
echo -e "  3. Update DOMAIN and SSL_EMAIL"
echo -e "  4. Configure optional integrations"
echo ""
echo -e "${RED}IMPORTANT: Keep this file secure!${NC}"
echo -e "${RED}Never commit .env to version control!${NC}"
echo ""
