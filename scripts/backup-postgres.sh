#!/bin/bash

###########################################
# ServiceDesk Platform - PostgreSQL Backup Script
# Creates database dumps with date stamps and rotation
# Add to cron: 0 2 * * * /path/to/backup-postgres.sh >> /var/log/pg-backup.log 2>&1
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
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_ROOT/backups}"

# Database connection (from environment or defaults)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-servicedesk}"
DB_USER="${DB_USER:-servicedesk}"
DB_PASSWORD="${DB_PASSWORD:-servicedesk}"

# Backup retention
RETENTION_DAYS="${RETENTION_DAYS:-7}"

# S3/MinIO configuration (optional)
S3_ENABLED="${S3_ENABLED:-false}"
S3_BUCKET="${S3_BUCKET:-servicedesk-backups}"
S3_ENDPOINT="${S3_ENDPOINT:-}"  # Leave empty for AWS S3
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-}"

# Timestamp
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DATE=$(date +%Y-%m-%d)
BACKUP_FILE="servicedesk_${DATE}_${TIMESTAMP}.sql.gz"
BACKUP_PATH="$BACKUP_DIR/$BACKUP_FILE"

# Logging
LOG_DATE=$(date '+%Y-%m-%d %H:%M:%S')

log_info() {
    echo -e "[$LOG_DATE] ${GREEN}INFO:${NC} $1"
}

log_warn() {
    echo -e "[$LOG_DATE] ${YELLOW}WARN:${NC} $1"
}

log_error() {
    echo -e "[$LOG_DATE] ${RED}ERROR:${NC} $1"
}

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk - PostgreSQL Backup${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Create backup directory
###########################################
log_info "Creating backup directory..."
mkdir -p "$BACKUP_DIR"

###########################################
# Create database dump
###########################################
log_info "Starting database backup..."
log_info "Database: $DB_NAME@$DB_HOST:$DB_PORT"

# Check if running in Docker environment
if docker ps --format '{{.Names}}' | grep -q servicedesk-postgres; then
    log_info "Using Docker container for backup..."
    
    docker exec -e PGPASSWORD="$DB_PASSWORD" servicedesk-postgres \
        pg_dump -U "$DB_USER" -d "$DB_NAME" \
        --format=plain \
        --no-owner \
        --no-privileges \
        --verbose 2>&1 | gzip > "$BACKUP_PATH"
else
    # Direct connection
    log_info "Using direct connection for backup..."
    
    export PGPASSWORD="$DB_PASSWORD"
    pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
        --format=plain \
        --no-owner \
        --no-privileges \
        --verbose 2>&1 | gzip > "$BACKUP_PATH"
    unset PGPASSWORD
fi

# Verify backup was created
if [ -f "$BACKUP_PATH" ]; then
    BACKUP_SIZE=$(du -h "$BACKUP_PATH" | cut -f1)
    log_info "Backup created successfully: $BACKUP_FILE ($BACKUP_SIZE)"
else
    log_error "Backup file was not created!"
    exit 1
fi

# Verify backup integrity
log_info "Verifying backup integrity..."
if gzip -t "$BACKUP_PATH" 2>/dev/null; then
    log_info "Backup integrity verified"
else
    log_error "Backup file is corrupted!"
    rm -f "$BACKUP_PATH"
    exit 1
fi

###########################################
# Create checksum
###########################################
log_info "Creating checksum..."
sha256sum "$BACKUP_PATH" > "$BACKUP_PATH.sha256"

###########################################
# Upload to S3/MinIO (if enabled)
###########################################
if [ "$S3_ENABLED" == "true" ]; then
    log_info "Uploading backup to S3/MinIO..."
    
    if ! command -v aws &> /dev/null; then
        log_warn "AWS CLI not installed, skipping S3 upload"
    else
        # Configure AWS CLI for MinIO if endpoint is specified
        if [ -n "$S3_ENDPOINT" ]; then
            AWS_ARGS="--endpoint-url $S3_ENDPOINT"
            export AWS_ACCESS_KEY_ID="$MINIO_ACCESS_KEY"
            export AWS_SECRET_ACCESS_KEY="$MINIO_SECRET_KEY"
        else
            AWS_ARGS=""
        fi
        
        # Upload backup and checksum
        aws s3 cp "$BACKUP_PATH" "s3://$S3_BUCKET/$(date +%Y/%m)/$BACKUP_FILE" $AWS_ARGS
        aws s3 cp "$BACKUP_PATH.sha256" "s3://$S3_BUCKET/$(date +%Y/%m)/$BACKUP_FILE.sha256" $AWS_ARGS
        
        if [ $? -eq 0 ]; then
            log_info "Backup uploaded to S3: s3://$S3_BUCKET/$(date +%Y/%m)/$BACKUP_FILE"
        else
            log_error "Failed to upload backup to S3"
        fi
        
        # Clean up credentials
        unset AWS_ACCESS_KEY_ID
        unset AWS_SECRET_ACCESS_KEY
    fi
fi

###########################################
# Rotate old backups
###########################################
log_info "Rotating old backups (keeping last $RETENTION_DAYS days)..."

# Find and remove old local backups
find "$BACKUP_DIR" -name "servicedesk_*.sql.gz" -type f -mtime +$RETENTION_DAYS -delete 2>/dev/null
find "$BACKUP_DIR" -name "servicedesk_*.sql.gz.sha256" -type f -mtime +$RETENTION_DAYS -delete 2>/dev/null

# Count remaining backups
BACKUP_COUNT=$(find "$BACKUP_DIR" -name "servicedesk_*.sql.gz" -type f | wc -l)
log_info "Local backups remaining: $BACKUP_COUNT"

# Rotate S3/MinIO backups if enabled
if [ "$S3_ENABLED" == "true" ] && command -v aws &> /dev/null; then
    log_info "Cleaning old S3 backups..."
    
    if [ -n "$S3_ENDPOINT" ]; then
        AWS_ARGS="--endpoint-url $S3_ENDPOINT"
        export AWS_ACCESS_KEY_ID="$MINIO_ACCESS_KEY"
        export AWS_SECRET_ACCESS_KEY="$MINIO_SECRET_KEY"
    else
        AWS_ARGS=""
    fi
    
    # List and remove old backups (older than retention period)
    OLD_DATE=$(date -d "-$RETENTION_DAYS days" +%Y-%m-%d)
    aws s3 ls "s3://$S3_BUCKET/" --recursive $AWS_ARGS | while read -r line; do
        FILE_DATE=$(echo "$line" | awk '{print $1}')
        FILE_PATH=$(echo "$line" | awk '{print $4}')
        if [[ "$FILE_DATE" < "$OLD_DATE" ]]; then
            aws s3 rm "s3://$S3_BUCKET/$FILE_PATH" $AWS_ARGS
            log_info "Deleted old S3 backup: $FILE_PATH"
        fi
    done
    
    unset AWS_ACCESS_KEY_ID
    unset AWS_SECRET_ACCESS_KEY
fi

###########################################
# Summary
###########################################
echo ""
echo -e "${BLUE}=====================================${NC}"
echo -e "${GREEN}Backup Complete!${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo -e "Backup file:     $BACKUP_FILE"
echo -e "Backup size:     $BACKUP_SIZE"
echo -e "Backup location: $BACKUP_DIR"
echo -e "Checksum:        $(cat "$BACKUP_PATH.sha256" | cut -d' ' -f1)"
if [ "$S3_ENABLED" == "true" ]; then
    echo -e "S3 location:     s3://$S3_BUCKET/$(date +%Y/%m)/$BACKUP_FILE"
fi
echo ""
echo -e "${YELLOW}To restore this backup:${NC}"
echo -e "  ./scripts/restore-postgres.sh $BACKUP_PATH"
echo ""
