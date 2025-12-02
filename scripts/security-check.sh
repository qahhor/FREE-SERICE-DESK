#!/bin/bash

###########################################
# ServiceDesk Platform - Security Check Script
# Validates security configuration before deployment
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

# Counters
WARNINGS=0
ERRORS=0

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}ServiceDesk - Security Check${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

###########################################
# Helper functions
###########################################
check_pass() {
    echo -e "${GREEN}✓ PASS:${NC} $1"
}

check_warn() {
    echo -e "${YELLOW}⚠ WARN:${NC} $1"
    ((WARNINGS++))
}

check_fail() {
    echo -e "${RED}✗ FAIL:${NC} $1"
    ((ERRORS++))
}

###########################################
# 1. Check .env file
###########################################
echo -e "${YELLOW}[1/8] Checking environment configuration...${NC}"

if [ ! -f "$ENV_FILE" ]; then
    check_fail ".env file not found"
else
    check_pass ".env file exists"
    
    # Source the env file
    set -a
    source "$ENV_FILE"
    set +a
    
    # Check JWT_SECRET
    if [ -z "$JWT_SECRET" ]; then
        check_fail "JWT_SECRET is not set"
    elif [ ${#JWT_SECRET} -lt 32 ]; then
        check_fail "JWT_SECRET is too short (min 32 characters)"
    elif [[ "$JWT_SECRET" == *"CHANGE"* ]] || [[ "$JWT_SECRET" == *"example"* ]]; then
        check_fail "JWT_SECRET contains default/example value"
    else
        check_pass "JWT_SECRET is properly configured"
    fi
    
    # Check DB_PASSWORD
    if [ -z "$DB_PASSWORD" ]; then
        check_fail "DB_PASSWORD is not set"
    elif [ ${#DB_PASSWORD} -lt 12 ]; then
        check_warn "DB_PASSWORD should be at least 12 characters"
    elif [[ "$DB_PASSWORD" == "servicedesk" ]] || [[ "$DB_PASSWORD" == "password" ]]; then
        check_fail "DB_PASSWORD is using a default value"
    else
        check_pass "DB_PASSWORD is properly configured"
    fi
    
    # Check GRAFANA_ADMIN_PASSWORD
    if [ -z "$GRAFANA_ADMIN_PASSWORD" ]; then
        check_warn "GRAFANA_ADMIN_PASSWORD is not set (will use default)"
    elif [[ "$GRAFANA_ADMIN_PASSWORD" == "admin" ]]; then
        check_warn "GRAFANA_ADMIN_PASSWORD is using default value"
    else
        check_pass "GRAFANA_ADMIN_PASSWORD is configured"
    fi
fi

echo ""

###########################################
# 2. Check SSL certificates
###########################################
echo -e "${YELLOW}[2/8] Checking SSL certificates...${NC}"

SSL_DIR="$PROJECT_ROOT/nginx/ssl"
CERT_FILE="$SSL_DIR/live/servicedesk/fullchain.pem"
KEY_FILE="$SSL_DIR/live/servicedesk/privkey.pem"

if [ ! -f "$CERT_FILE" ]; then
    check_warn "SSL certificate not found (may be configured later)"
else
    check_pass "SSL certificate exists"
    
    # Check certificate expiration
    EXPIRY=$(openssl x509 -in "$CERT_FILE" -noout -enddate 2>/dev/null | cut -d= -f2)
    EXPIRY_EPOCH=$(date -d "$EXPIRY" +%s 2>/dev/null || echo "0")
    NOW_EPOCH=$(date +%s)
    DAYS_LEFT=$(( (EXPIRY_EPOCH - NOW_EPOCH) / 86400 ))
    
    if [ "$DAYS_LEFT" -lt 0 ]; then
        check_fail "SSL certificate has expired!"
    elif [ "$DAYS_LEFT" -lt 7 ]; then
        check_fail "SSL certificate expires in $DAYS_LEFT days"
    elif [ "$DAYS_LEFT" -lt 30 ]; then
        check_warn "SSL certificate expires in $DAYS_LEFT days"
    else
        check_pass "SSL certificate valid for $DAYS_LEFT days"
    fi
fi

if [ ! -f "$KEY_FILE" ]; then
    check_warn "SSL private key not found"
else
    check_pass "SSL private key exists"
    
    # Check key permissions
    KEY_PERMS=$(stat -c %a "$KEY_FILE" 2>/dev/null || echo "unknown")
    if [ "$KEY_PERMS" != "600" ] && [ "$KEY_PERMS" != "400" ]; then
        check_warn "SSL private key has loose permissions ($KEY_PERMS, should be 600)"
    else
        check_pass "SSL private key has correct permissions"
    fi
fi

echo ""

###########################################
# 3. Check Docker configuration
###########################################
echo -e "${YELLOW}[3/8] Checking Docker configuration...${NC}"

if ! command -v docker &> /dev/null; then
    check_fail "Docker is not installed"
else
    check_pass "Docker is installed"
    
    # Check Docker socket permissions
    if [ -S /var/run/docker.sock ]; then
        DOCKER_PERMS=$(stat -c %a /var/run/docker.sock 2>/dev/null || echo "unknown")
        if [ "$DOCKER_PERMS" == "777" ]; then
            check_warn "Docker socket has overly permissive access"
        else
            check_pass "Docker socket permissions are reasonable"
        fi
    fi
fi

echo ""

###########################################
# 4. Check for sensitive files in git
###########################################
echo -e "${YELLOW}[4/8] Checking for sensitive files...${NC}"

cd "$PROJECT_ROOT"

# Check .gitignore
if [ ! -f ".gitignore" ]; then
    check_warn ".gitignore file not found"
else
    if grep -q "^\.env$" .gitignore; then
        check_pass ".env is in .gitignore"
    else
        check_fail ".env is NOT in .gitignore"
    fi
    
    if grep -q "ssl" .gitignore || grep -q "*.pem" .gitignore; then
        check_pass "SSL files are in .gitignore"
    else
        check_warn "SSL files may not be properly ignored"
    fi
fi

# Check if .env is tracked by git
if git ls-files --error-unmatch .env &> /dev/null; then
    check_fail ".env file is tracked by git!"
else
    check_pass ".env file is not tracked by git"
fi

echo ""

###########################################
# 5. Check network exposure
###########################################
echo -e "${YELLOW}[5/8] Checking network exposure...${NC}"

# Check if database ports are exposed externally
if docker ps --format '{{.Ports}}' 2>/dev/null | grep -q "0.0.0.0:5432"; then
    check_warn "PostgreSQL port is exposed to all interfaces"
else
    check_pass "PostgreSQL port is not publicly exposed"
fi

if docker ps --format '{{.Ports}}' 2>/dev/null | grep -q "0.0.0.0:6379"; then
    check_warn "Redis port is exposed to all interfaces"
else
    check_pass "Redis port is not publicly exposed"
fi

if docker ps --format '{{.Ports}}' 2>/dev/null | grep -q "0.0.0.0:9200"; then
    check_warn "Elasticsearch port is exposed to all interfaces"
else
    check_pass "Elasticsearch port is not publicly exposed"
fi

echo ""

###########################################
# 6. Check security headers (if running)
###########################################
echo -e "${YELLOW}[6/8] Checking security headers...${NC}"

if curl -s -o /dev/null -w "%{http_code}" http://localhost:80 2>/dev/null | grep -q "200\|301\|302"; then
    # Check security headers
    HEADERS=$(curl -sI http://localhost 2>/dev/null)
    
    if echo "$HEADERS" | grep -qi "X-Frame-Options"; then
        check_pass "X-Frame-Options header is set"
    else
        check_warn "X-Frame-Options header is missing"
    fi
    
    if echo "$HEADERS" | grep -qi "X-Content-Type-Options"; then
        check_pass "X-Content-Type-Options header is set"
    else
        check_warn "X-Content-Type-Options header is missing"
    fi
    
    if echo "$HEADERS" | grep -qi "Strict-Transport-Security"; then
        check_pass "HSTS header is set"
    else
        check_warn "HSTS header is missing"
    fi
else
    check_warn "Application not running, skipping header checks"
fi

echo ""

###########################################
# 7. Check file permissions
###########################################
echo -e "${YELLOW}[7/8] Checking file permissions...${NC}"

# Check scripts are executable
for script in "$PROJECT_ROOT/scripts/"*.sh; do
    if [ -f "$script" ] && [ -x "$script" ]; then
        check_pass "$(basename "$script") is executable"
    elif [ -f "$script" ]; then
        check_warn "$(basename "$script") is not executable"
    fi
done

# Check .env permissions
if [ -f "$ENV_FILE" ]; then
    ENV_PERMS=$(stat -c %a "$ENV_FILE" 2>/dev/null || echo "unknown")
    if [ "$ENV_PERMS" == "600" ] || [ "$ENV_PERMS" == "400" ]; then
        check_pass ".env has secure permissions ($ENV_PERMS)"
    else
        check_warn ".env has loose permissions ($ENV_PERMS, should be 600)"
    fi
fi

echo ""

###########################################
# 8. Check for common vulnerabilities
###########################################
echo -e "${YELLOW}[8/8] Checking for common issues...${NC}"

# Check for debug mode
if [ "$SPRING_PROFILES_ACTIVE" == "dev" ] || [ "$DEBUG" == "true" ]; then
    check_warn "Application may be in debug mode"
else
    check_pass "Application is not in debug mode"
fi

# Check for default credentials in compose files
for compose_file in "$PROJECT_ROOT"/docker-compose*.yml; do
    if [ -f "$compose_file" ] && grep -q "password123\|admin123\|secret" "$compose_file"; then
        check_warn "$(basename "$compose_file") may contain default credentials"
    fi
done

echo ""

###########################################
# Summary
###########################################
echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}Security Check Summary${NC}"
echo -e "${BLUE}=====================================${NC}"
echo ""

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}ERRORS: $ERRORS${NC}"
fi

if [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}WARNINGS: $WARNINGS${NC}"
fi

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}All security checks passed!${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}Security check completed with warnings.${NC}"
    echo -e "Review the warnings above before deploying to production."
    exit 0
else
    echo -e "${RED}Security check failed!${NC}"
    echo -e "Please fix the errors above before deploying to production."
    exit 1
fi
