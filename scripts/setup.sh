#!/bin/bash

# ServiceDesk Platform Setup Script
# This script sets up the development environment

set -e

echo "========================================="
echo "ServiceDesk Platform - Development Setup"
echo "========================================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
check_prerequisites() {
    echo -e "\n${YELLOW}Checking prerequisites...${NC}"

    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            echo -e "${GREEN}✓ Java $JAVA_VERSION found${NC}"
        else
            echo -e "${RED}✗ Java 17+ required (found $JAVA_VERSION)${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Java not found${NC}"
        exit 1
    fi

    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
        echo -e "${GREEN}✓ Maven $MVN_VERSION found${NC}"
    else
        echo -e "${RED}✗ Maven not found${NC}"
        exit 1
    fi

    # Check Docker
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
        echo -e "${GREEN}✓ Docker $DOCKER_VERSION found${NC}"
    else
        echo -e "${RED}✗ Docker not found${NC}"
        exit 1
    fi

    # Check Docker Compose
    if command -v docker-compose &> /dev/null; then
        COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | sed 's/,//')
        echo -e "${GREEN}✓ Docker Compose $COMPOSE_VERSION found${NC}"
    else
        echo -e "${RED}✗ Docker Compose not found${NC}"
        exit 1
    fi
}

# Start infrastructure services
start_infrastructure() {
    echo -e "\n${YELLOW}Starting infrastructure services...${NC}"

    docker-compose -f docker-compose.dev.yml up -d

    echo -e "${GREEN}✓ Infrastructure services started${NC}"
    echo "  - PostgreSQL: localhost:5432"
    echo "  - Redis: localhost:6379"
    echo "  - MailHog: localhost:8025"
}

# Wait for PostgreSQL
wait_for_postgres() {
    echo -e "\n${YELLOW}Waiting for PostgreSQL...${NC}"

    until docker exec servicedesk-postgres pg_isready -U servicedesk -d servicedesk > /dev/null 2>&1; do
        echo "  Waiting for PostgreSQL to be ready..."
        sleep 2
    done

    echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
}

# Build backend
build_backend() {
    echo -e "\n${YELLOW}Building backend...${NC}"

    cd backend
    mvn clean install -DskipTests
    cd ..

    echo -e "${GREEN}✓ Backend built successfully${NC}"
}

# Run database migrations
run_migrations() {
    echo -e "\n${YELLOW}Running database migrations...${NC}"

    cd backend/ticket-service
    mvn flyway:migrate
    cd ../..

    echo -e "${GREEN}✓ Migrations completed${NC}"
}

# Print success message
print_success() {
    echo -e "\n${GREEN}=========================================${NC}"
    echo -e "${GREEN}Setup completed successfully!${NC}"
    echo -e "${GREEN}=========================================${NC}"
    echo ""
    echo "To start the application:"
    echo "  cd backend/ticket-service"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Access points:"
    echo "  - API: http://localhost:8080"
    echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
    echo "  - MailHog: http://localhost:8025"
    echo ""
    echo "Default credentials:"
    echo "  - Admin: admin@servicedesk.local / admin123"
    echo "  - Agent: agent1@servicedesk.local / admin123"
    echo ""
}

# Main
main() {
    check_prerequisites
    start_infrastructure
    wait_for_postgres
    build_backend
    print_success
}

main "$@"
