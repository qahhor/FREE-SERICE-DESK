#!/bin/bash

###########################################
# ServiceDesk Platform - PostgreSQL Restore Script
# Restores database from backup files
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

usage() {
    echo "Usage: $0 <backup_file> [options]"
    echo ""
    echo "Arguments:"
    echo "  backup_file    Path to backup file (.sql.gz)"
    echo ""
    echo "Options:"
    echo "  --force        Skip confirmation prompt"
    echo "  --drop-db      Drop and recreate database before restore"
    echo "  --list         List available backups"
    echo "  --verify       Only verify backup file integrity"
    echo "  -h, --help     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 backups/servicedesk_2024-01-15_020000.sql.gz"
    echo "  $0 backups/servicedesk_2024-01-15_020000.sql.gz --force"
    echo "  $0 --list"
    echo ""
    echo "Environment variables:"
    echo "  DB_HOST        Database host (default: localhost)"
    echo "  DB_PORT        Database port (default: 5432)"
    echo "  DB_NAME        Database name (default: servicedesk)"
    echo "  DB_USER        Database user (default: servicedesk)"
    echo "  DB_PASSWORD    Database password"
    exit 1
}

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk - PostgreSQL Restore${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Parse arguments
###########################################
BACKUP_FILE=""
FORCE=false
DROP_DB=false
LIST_ONLY=false
VERIFY_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --force)
            FORCE=true
            shift
            ;;
        --drop-db)
            DROP_DB=true
            shift
            ;;
        --list)
            LIST_ONLY=true
            shift
            ;;
        --verify)
            VERIFY_ONLY=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            if [ -z "$BACKUP_FILE" ]; then
                BACKUP_FILE="$1"
            else
                echo -e "${RED}Unknown argument: $1${NC}"
                usage
            fi
            shift
            ;;
    esac
done

###########################################
# List available backups
###########################################
if [ "$LIST_ONLY" == "true" ]; then
    log_info "Available backups in $BACKUP_DIR:"
    echo ""
    
    if [ -d "$BACKUP_DIR" ]; then
        ls -lh "$BACKUP_DIR"/*.sql.gz 2>/dev/null | while read line; do
            echo "  $line"
        done
        
        if [ $(ls -1 "$BACKUP_DIR"/*.sql.gz 2>/dev/null | wc -l) -eq 0 ]; then
            echo "  No backups found"
        fi
    else
        echo "  Backup directory does not exist"
    fi
    echo ""
    exit 0
fi

###########################################
# Validate backup file
###########################################
if [ -z "$BACKUP_FILE" ]; then
    log_error "Backup file is required"
    usage
fi

if [ ! -f "$BACKUP_FILE" ]; then
    log_error "Backup file not found: $BACKUP_FILE"
    exit 1
fi

log_info "Backup file: $BACKUP_FILE"
log_info "File size: $(du -h "$BACKUP_FILE" | cut -f1)"

###########################################
# Verify backup integrity
###########################################
log_info "Verifying backup integrity..."

if gzip -t "$BACKUP_FILE" 2>/dev/null; then
    log_info "Gzip integrity check passed"
else
    log_error "Backup file is corrupted (gzip check failed)"
    exit 1
fi

# Check SHA256 if checksum file exists
CHECKSUM_FILE="$BACKUP_FILE.sha256"
if [ -f "$CHECKSUM_FILE" ]; then
    log_info "Verifying SHA256 checksum..."
    if sha256sum -c "$CHECKSUM_FILE" --status 2>/dev/null; then
        log_info "SHA256 checksum verified"
    else
        log_error "SHA256 checksum verification failed!"
        exit 1
    fi
else
    log_warn "No checksum file found, skipping verification"
fi

if [ "$VERIFY_ONLY" == "true" ]; then
    log_info "Verification complete"
    exit 0
fi

###########################################
# Confirmation
###########################################
if [ "$FORCE" != "true" ]; then
    echo ""
    echo -e "${YELLOW}WARNING: This will restore the database from backup.${NC}"
    echo -e "${YELLOW}All current data will be overwritten!${NC}"
    echo ""
    echo -e "Database: ${RED}$DB_NAME${NC}@${DB_HOST}:${DB_PORT}"
    echo -e "Backup:   ${GREEN}$(basename "$BACKUP_FILE")${NC}"
    echo ""
    read -p "Are you sure you want to continue? (yes/no): " CONFIRM
    
    if [ "$CONFIRM" != "yes" ]; then
        log_info "Restore cancelled"
        exit 0
    fi
fi

###########################################
# Stop application (if running)
###########################################
log_info "Checking if application is running..."

if docker ps --format '{{.Names}}' | grep -q servicedesk-monolith; then
    log_warn "Application is running. It's recommended to stop it during restore."
    
    if [ "$FORCE" != "true" ]; then
        read -p "Stop application? (yes/no): " STOP_APP
        if [ "$STOP_APP" == "yes" ]; then
            docker stop servicedesk-monolith
            log_info "Application stopped"
        fi
    fi
fi

###########################################
# Drop and recreate database (if requested)
###########################################
if [ "$DROP_DB" == "true" ]; then
    log_warn "Dropping and recreating database..."
    
    if docker ps --format '{{.Names}}' | grep -q servicedesk-postgres; then
        docker exec -e PGPASSWORD="$DB_PASSWORD" servicedesk-postgres \
            psql -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;"
        docker exec -e PGPASSWORD="$DB_PASSWORD" servicedesk-postgres \
            psql -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;"
        log_info "Database recreated"
    else
        export PGPASSWORD="$DB_PASSWORD"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
            -c "DROP DATABASE IF EXISTS $DB_NAME;"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
            -c "CREATE DATABASE $DB_NAME;"
        unset PGPASSWORD
        log_info "Database recreated"
    fi
fi

###########################################
# Restore database
###########################################
log_info "Restoring database..."

if docker ps --format '{{.Names}}' | grep -q servicedesk-postgres; then
    log_info "Using Docker container for restore..."
    
    gunzip -c "$BACKUP_FILE" | docker exec -i -e PGPASSWORD="$DB_PASSWORD" servicedesk-postgres \
        psql -U "$DB_USER" -d "$DB_NAME" --quiet
else
    log_info "Using direct connection for restore..."
    
    export PGPASSWORD="$DB_PASSWORD"
    gunzip -c "$BACKUP_FILE" | psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" --quiet
    unset PGPASSWORD
fi

if [ $? -eq 0 ]; then
    log_info "Database restored successfully"
else
    log_error "Database restore failed"
    exit 1
fi

###########################################
# Restart application (if it was running)
###########################################
if docker ps -a --format '{{.Names}}' | grep -q servicedesk-monolith; then
    if ! docker ps --format '{{.Names}}' | grep -q servicedesk-monolith; then
        log_info "Starting application..."
        docker start servicedesk-monolith
        log_info "Application started"
    fi
fi

###########################################
# Summary
###########################################
echo ""
echo -e "${BLUE}=====================================${NC}"
echo -e "${GREEN}Restore Complete!${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""
echo -e "Restored from: $(basename "$BACKUP_FILE")"
echo -e "Database:      $DB_NAME@$DB_HOST:$DB_PORT"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Verify application is working correctly"
echo -e "  2. Check database integrity"
echo -e "  3. Test critical functionality"
echo ""
