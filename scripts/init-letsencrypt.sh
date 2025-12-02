#!/bin/bash

###########################################
# ServiceDesk Platform - Let's Encrypt SSL Initialization
# First-time SSL certificate generation with Certbot
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
SSL_DIR="$PROJECT_ROOT/nginx/ssl"
CERTBOT_WWW="$PROJECT_ROOT/certbot/www"

# Default values (override via environment variables)
DOMAIN="${DOMAIN:-servicedesk.example.com}"
EMAIL="${EMAIL:-admin@example.com}"
STAGING="${STAGING:-0}"  # Set to 1 for Let's Encrypt staging environment

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk - Let's Encrypt SSL Setup${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Check prerequisites
###########################################
echo -e "${YELLOW}[1/7] Checking prerequisites...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}ERROR: Docker is not installed${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}ERROR: Docker Compose is not installed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker and Docker Compose found${NC}"
echo ""

###########################################
# Validate domain and email
###########################################
echo -e "${YELLOW}[2/7] Validating configuration...${NC}"

if [ "$DOMAIN" == "servicedesk.example.com" ]; then
    echo -e "${RED}ERROR: Please set DOMAIN environment variable${NC}"
    echo -e "${YELLOW}Usage: DOMAIN=yourdomain.com EMAIL=your@email.com ./init-letsencrypt.sh${NC}"
    exit 1
fi

if [ "$EMAIL" == "admin@example.com" ]; then
    echo -e "${RED}ERROR: Please set EMAIL environment variable${NC}"
    echo -e "${YELLOW}Usage: DOMAIN=yourdomain.com EMAIL=your@email.com ./init-letsencrypt.sh${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Domain: $DOMAIN${NC}"
echo -e "${GREEN}✓ Email: $EMAIL${NC}"
echo ""

###########################################
# Create directories
###########################################
echo -e "${YELLOW}[3/7] Creating directories...${NC}"

mkdir -p "$SSL_DIR"
mkdir -p "$CERTBOT_WWW"

echo -e "${GREEN}✓ Directories created${NC}"
echo ""

###########################################
# Generate DH parameters
###########################################
echo -e "${YELLOW}[4/7] Generating DH parameters (this may take a while)...${NC}"

if [ ! -f "$SSL_DIR/dhparam.pem" ]; then
    openssl dhparam -out "$SSL_DIR/dhparam.pem" 2048
    echo -e "${GREEN}✓ DH parameters generated${NC}"
else
    echo -e "${GREEN}✓ DH parameters already exist${NC}"
fi
echo ""

###########################################
# Create temporary self-signed certificate
###########################################
echo -e "${YELLOW}[5/7] Creating temporary self-signed certificate...${NC}"

# Create directory structure matching Let's Encrypt
mkdir -p "$SSL_DIR/live/$DOMAIN"

# Generate self-signed certificate for initial nginx startup
if [ ! -f "$SSL_DIR/live/$DOMAIN/fullchain.pem" ]; then
    openssl req -x509 -nodes -newkey rsa:4096 -days 1 \
        -keyout "$SSL_DIR/live/$DOMAIN/privkey.pem" \
        -out "$SSL_DIR/live/$DOMAIN/fullchain.pem" \
        -subj "/CN=$DOMAIN"
    echo -e "${GREEN}✓ Temporary certificate created${NC}"
else
    echo -e "${GREEN}✓ Certificate already exists${NC}"
fi
echo ""

###########################################
# Update nginx configuration
###########################################
echo -e "${YELLOW}[6/7] Updating nginx configuration...${NC}"

# Create symlink for servicedesk (generic name used in nginx config)
if [ ! -L "$SSL_DIR/live/servicedesk" ] && [ ! -d "$SSL_DIR/live/servicedesk" ]; then
    ln -sf "$DOMAIN" "$SSL_DIR/live/servicedesk"
    echo -e "${GREEN}✓ Symlink created${NC}"
else
    echo -e "${GREEN}✓ Symlink already exists${NC}"
fi
echo ""

###########################################
# Request Let's Encrypt certificate
###########################################
echo -e "${YELLOW}[7/7] Requesting Let's Encrypt certificate...${NC}"

# Start nginx to handle ACME challenge
cd "$PROJECT_ROOT"

# Check if nginx is running
if docker ps --format '{{.Names}}' | grep -q servicedesk-nginx; then
    echo -e "${YELLOW}Reloading nginx...${NC}"
    docker exec servicedesk-nginx nginx -s reload
else
    echo -e "${YELLOW}Starting nginx...${NC}"
    docker-compose -f docker-compose.prod.yml up -d nginx
    sleep 5
fi

# Set staging flag
STAGING_FLAG=""
if [ "$STAGING" == "1" ]; then
    STAGING_FLAG="--staging"
    echo -e "${YELLOW}Using Let's Encrypt staging environment${NC}"
fi

# Request certificate
docker run --rm -it \
    -v "$SSL_DIR:/etc/letsencrypt" \
    -v "$CERTBOT_WWW:/var/www/certbot" \
    certbot/certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    $STAGING_FLAG \
    -d "$DOMAIN"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SSL certificate obtained successfully!${NC}"
    
    # Update symlink to point to new certificate
    rm -f "$SSL_DIR/live/servicedesk"
    ln -sf "$DOMAIN" "$SSL_DIR/live/servicedesk"
    
    # Reload nginx to use new certificate
    if docker ps --format '{{.Names}}' | grep -q servicedesk-nginx; then
        docker exec servicedesk-nginx nginx -s reload
        echo -e "${GREEN}✓ Nginx reloaded with new certificate${NC}"
    fi
else
    echo -e "${RED}ERROR: Failed to obtain SSL certificate${NC}"
    echo -e "${YELLOW}Make sure:${NC}"
    echo -e "  1. Domain $DOMAIN points to this server"
    echo -e "  2. Port 80 is accessible from the internet"
    echo -e "  3. No firewall is blocking Let's Encrypt"
    exit 1
fi

echo ""
echo -e "${BLUE}=====================================${NC}"
echo -e "${GREEN}SSL Setup Complete!${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Restart all services: docker-compose -f docker-compose.prod.yml up -d"
echo -e "  2. Set up automatic renewal: crontab -e"
echo -e "     Add: 0 0 * * * /path/to/scripts/renew-ssl.sh >> /var/log/certbot-renew.log 2>&1"
echo ""
echo -e "${GREEN}Certificate location: $SSL_DIR/live/$DOMAIN${NC}"
echo ""
