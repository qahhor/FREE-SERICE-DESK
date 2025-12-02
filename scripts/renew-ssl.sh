#!/bin/bash

###########################################
# ServiceDesk Platform - SSL Certificate Renewal
# Automatic renewal script for Let's Encrypt certificates
# Add to cron: 0 0 * * * /path/to/renew-ssl.sh >> /var/log/certbot-renew.log 2>&1
###########################################

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SSL_DIR="$PROJECT_ROOT/nginx/ssl"
CERTBOT_WWW="$PROJECT_ROOT/certbot/www"

# Logging
LOG_DATE=$(date '+%Y-%m-%d %H:%M:%S')

echo "[$LOG_DATE] Starting SSL certificate renewal check..."

# Check if certbot directories exist
if [ ! -d "$SSL_DIR" ]; then
    echo "[$LOG_DATE] ERROR: SSL directory not found: $SSL_DIR"
    exit 1
fi

# Run certbot renewal
docker run --rm \
    -v "$SSL_DIR:/etc/letsencrypt" \
    -v "$CERTBOT_WWW:/var/www/certbot" \
    certbot/certbot renew \
    --webroot \
    --webroot-path=/var/www/certbot \
    --quiet \
    --deploy-hook "echo 'Certificate renewed'"

RENEWAL_EXIT_CODE=$?

if [ $RENEWAL_EXIT_CODE -eq 0 ]; then
    echo "[$LOG_DATE] Certificate renewal check completed successfully"
    
    # Reload nginx to apply new certificate (if renewed)
    if docker ps --format '{{.Names}}' | grep -q servicedesk-nginx; then
        docker exec servicedesk-nginx nginx -s reload
        echo "[$LOG_DATE] Nginx reloaded"
    else
        echo "[$LOG_DATE] WARNING: Nginx container not running"
    fi
else
    echo "[$LOG_DATE] ERROR: Certificate renewal failed with exit code $RENEWAL_EXIT_CODE"
    
    # Send alert (customize as needed)
    # mail -s "SSL Renewal Failed - ServiceDesk" admin@example.com <<< "Certificate renewal failed on $(hostname)"
    
    exit 1
fi

echo "[$LOG_DATE] SSL renewal script completed"
