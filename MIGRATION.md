# Migration Guide: Microservices to Monolith

This guide provides detailed instructions for migrating from the microservices architecture to the unified monolithic application.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Pre-Migration Checklist](#pre-migration-checklist)
- [Migration Steps](#migration-steps)
- [Data Migration](#data-migration)
- [Verification](#verification)
- [Rollback Plan](#rollback-plan)
- [Post-Migration Tasks](#post-migration-tasks)
- [Troubleshooting](#troubleshooting)

## Overview

### What's Changing

| Component | Microservices | Monolith | Impact |
|-----------|--------------|----------|---------|
| **Architecture** | 9 services | 1 service | Simplified deployment |
| **Databases** | 5 separate DBs | 1 unified DB | Data consolidation required |
| **Communication** | RabbitMQ + REST | Direct calls + Events | No message broker needed |
| **Deployment** | 9 containers | 1 container | Reduced complexity |
| **API Gateway** | Required | Not needed | One less component |
| **Memory** | ~4GB | ~1.5GB | 63% reduction |
| **Startup Time** | ~90s | ~30s | 3x faster |

### Migration Duration

- **Small deployment** (< 10GB data): 2-4 hours
- **Medium deployment** (10-100GB data): 4-8 hours
- **Large deployment** (> 100GB data): 8-24 hours

## Prerequisites

### System Requirements

- Docker & Docker Compose installed
- PostgreSQL 16 client tools (`psql`, `pg_dump`, `pg_restore`)
- Minimum 4GB free disk space (for backups)
- Access to all existing microservice databases
- Network connectivity to all services

### Required Access

- Root/sudo access to servers
- Database credentials for all microservices
- RabbitMQ admin access (for cleanup)
- Backup storage location

## Pre-Migration Checklist

### 1. Backup Everything

```bash
# Create backup directory
mkdir -p /backups/servicedesk-$(date +%Y%m%d)
cd /backups/servicedesk-$(date +%Y%m%d)

# Backup each microservice database
pg_dump -h localhost -U servicedesk -d ticket_db > ticket_db.sql
pg_dump -h localhost -U servicedesk -d channel_db > channel_db.sql
pg_dump -h localhost -U servicedesk -d notification_db > notification_db.sql
pg_dump -h localhost -U servicedesk -d knowledge_db > knowledge_db.sql
pg_dump -h localhost -U servicedesk -d marketplace_db > marketplace_db.sql

# Backup RabbitMQ definitions
docker exec servicedesk-rabbitmq rabbitmqctl export_definitions rabbitmq-definitions.json

# Backup application configurations
cp -r /path/to/servicedesk-platform .

# Verify backups
ls -lh
```

### 2. Document Current State

```bash
# Get current service versions
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"

# Check database sizes
psql -h localhost -U servicedesk -d ticket_db -c "
  SELECT
    pg_database.datname,
    pg_size_pretty(pg_database_size(pg_database.datname)) AS size
  FROM pg_database;"

# Export current metrics (if using Prometheus)
# Note current throughput, response times, error rates
```

### 3. Schedule Maintenance Window

- **Recommended**: 4-hour window during low-traffic period
- Notify users of planned downtime
- Disable monitoring alerts to avoid noise
- Prepare rollback plan

### 4. Prepare Monolith Infrastructure

```bash
# Clone/pull latest monolith version
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform
git checkout main  # or appropriate branch

# Copy environment configuration
cp .env.example .env

# Edit .env - set all required variables
nano .env
```

## Migration Steps

### Step 1: Stop Microservices

```bash
# Navigate to microservices directory
cd /path/to/servicedesk-microservices

# Stop all services gracefully
docker-compose down

# Verify all services are stopped
docker ps | grep servicedesk

# Keep infrastructure (postgres, redis, elasticsearch) running
docker-compose up -d postgres redis elasticsearch
```

### Step 2: Create Unified Database

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres

# Create unified database
CREATE DATABASE servicedesk_monolith;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_monolith TO servicedesk;

# Exit psql
\q
```

### Step 3: Migrate Data

#### Option A: Using Migration Script (Recommended)

```bash
# Run the automated migration script
cd /path/to/servicedesk-platform
chmod +x scripts/migrate-to-monolith.sh
./scripts/migrate-to-monolith.sh

# The script will:
# 1. Create schema in unified database
# 2. Migrate data from each microservice DB
# 3. Update foreign key references
# 4. Verify data integrity
```

#### Option B: Manual Migration

```bash
# 1. Import Ticket Service data
psql -h localhost -U servicedesk -d servicedesk_monolith < /backups/ticket_db.sql

# 2. Import Channel Service data
psql -h localhost -U servicedesk -d servicedesk_monolith < /backups/channel_db.sql

# 3. Import Notification Service data
psql -h localhost -U servicedesk -d servicedesk_monolith < /backups/notification_db.sql

# 4. Import Knowledge Service data
psql -h localhost -U servicedesk -d servicedesk_monolith < /backups/knowledge_db.sql

# 5. Import Marketplace Service data
psql -h localhost -U servicedesk -d servicedesk_monolith < /backups/marketplace_db.sql

# 6. Verify data
psql -h localhost -U servicedesk -d servicedesk_monolith -c "
  SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
  FROM pg_tables
  WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
  ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
  LIMIT 20;"
```

### Step 4: Update Configuration

```bash
cd /path/to/servicedesk-platform

# Edit .env file
nano .env
```

Required environment variables:

```bash
# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=production

# Database (unified)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=servicedesk_monolith
DB_USERNAME=servicedesk
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Elasticsearch
ELASTICSEARCH_URIS=http://localhost:9200

# Security (IMPORTANT: Generate new secret)
JWT_SECRET=your-very-secure-jwt-secret-key-at-least-32-characters-long

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@domain.com
MAIL_PASSWORD=your-app-password

# Optional: AI Features
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...

# Optional: Integrations
TELEGRAM_BOT_TOKEN=...
WHATSAPP_API_KEY=...
```

### Step 5: Deploy Monolith

```bash
cd /path/to/servicedesk-platform

# Build and start monolith
docker-compose -f docker-compose.monolith.yml up -d

# Monitor startup logs
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith

# Wait for "Started ServiceDeskMonolithApplication"
```

### Step 6: Verify Deployment

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}

# Check application info
curl http://localhost:8080/actuator/info

# Test authentication
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@servicedesk.local",
    "password": "admin123"
  }'

# Test a key endpoint
curl http://localhost:8080/api/v1/tickets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Data Migration

### Database Schema Mapping

The monolith uses a unified schema with all tables in one database:

| Microservice | Tables | Migration Version |
|-------------|--------|-------------------|
| Ticket Service | users, teams, tickets, sla_policies, assets, changes, problems | V1-V7 |
| Channel Service | channels, messages, widget_config | V10-V12 |
| Notification Service | notifications, notification_templates | V20-V21 |
| Knowledge Service | articles, categories | V30 |
| Marketplace Service | modules, plugins | V40-V41 |

### Handling Data Conflicts

If you encounter table name conflicts:

```sql
-- Example: Rename conflicting table
ALTER TABLE old_users RENAME TO users_backup;

-- Then import new schema
-- After verification, drop backup:
DROP TABLE users_backup;
```

### Foreign Key Updates

Some foreign keys may need updates if they reference data across services:

```sql
-- Example: Update ticket assignee references
UPDATE tickets t
SET assignee_id = u.id
FROM users u
WHERE t.assignee_email = u.email;
```

### Data Validation Queries

```sql
-- Check record counts
SELECT 'tickets' as table_name, COUNT(*) as count FROM tickets
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'channels', COUNT(*) FROM channels
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'articles', COUNT(*) FROM articles;

-- Check for orphaned records
SELECT COUNT(*) as orphaned_tickets
FROM tickets t
LEFT JOIN users u ON t.assignee_id = u.id
WHERE t.assignee_id IS NOT NULL AND u.id IS NULL;

-- Verify foreign key integrity
SELECT
  conname AS constraint_name,
  conrelid::regclass AS table_name,
  confrelid::regclass AS referenced_table
FROM pg_constraint
WHERE contype = 'f';
```

## Verification

### Functional Testing Checklist

- [ ] User authentication works
- [ ] Ticket creation/update/deletion
- [ ] Team and user management
- [ ] Channel integration (email, telegram, etc.)
- [ ] Notifications are sent
- [ ] Knowledge base search works
- [ ] AI features respond (if enabled)
- [ ] Analytics dashboards load
- [ ] File uploads/downloads work
- [ ] SLA rules trigger correctly
- [ ] Automation rules execute

### Performance Testing

```bash
# Test API response times
ab -n 1000 -c 10 http://localhost:8080/api/v1/tickets

# Monitor memory usage
docker stats servicedesk-monolith

# Check database connections
psql -h localhost -U servicedesk -d servicedesk_monolith -c "
  SELECT count(*) as active_connections
  FROM pg_stat_activity
  WHERE datname = 'servicedesk_monolith';"
```

### Monitoring Setup

```bash
# Access Prometheus
open http://localhost:9090

# Access Grafana
open http://localhost:3000
# Login: admin/admin

# Import ServiceDesk dashboard
# Dashboard JSON: infrastructure/grafana/dashboards/servicedesk-monolith.json
```

## Rollback Plan

If migration fails or issues are discovered:

### Quick Rollback (< 1 hour after migration)

```bash
# 1. Stop monolith
cd /path/to/servicedesk-platform
docker-compose -f docker-compose.monolith.yml down

# 2. Restore microservices
cd /path/to/servicedesk-microservices
docker-compose up -d

# 3. Verify all services are running
docker ps | grep servicedesk

# 4. Test key endpoints
curl http://localhost:8081/actuator/health  # Ticket Service
curl http://localhost:8082/actuator/health  # Channel Service
```

### Full Rollback (with data restoration)

```bash
# 1. Stop monolith
docker-compose -f docker-compose.monolith.yml down

# 2. Drop monolith database
psql -h localhost -U postgres -c "DROP DATABASE servicedesk_monolith;"

# 3. Restore original databases
psql -h localhost -U postgres -c "CREATE DATABASE ticket_db;"
psql -h localhost -U servicedesk -d ticket_db < /backups/ticket_db.sql

psql -h localhost -U postgres -c "CREATE DATABASE channel_db;"
psql -h localhost -U servicedesk -d channel_db < /backups/channel_db.sql

# Repeat for other databases...

# 4. Start microservices
cd /path/to/servicedesk-microservices
docker-compose up -d

# 5. Verify functionality
./scripts/health-check-all-services.sh
```

## Post-Migration Tasks

### 1. Update DNS/Load Balancer

If using external load balancer:

```bash
# Update upstream to point to monolith
# Before:
#   - ticket-service:8081
#   - channel-service:8082
#   - knowledge-service:8083
#   - ...
# After:
#   - servicedesk-monolith:8080
```

### 2. Decommission Microservices

```bash
# After 1-2 weeks of stable operation

# Stop microservices
cd /path/to/servicedesk-microservices
docker-compose down

# Remove containers and volumes
docker-compose down -v

# Archive microservice databases
pg_dump -h localhost -U servicedesk -d ticket_db | gzip > /archive/ticket_db_final.sql.gz
# Repeat for other databases

# Drop old databases
psql -h localhost -U postgres -c "DROP DATABASE ticket_db;"
psql -h localhost -U postgres -c "DROP DATABASE channel_db;"
# Repeat for other databases
```

### 3. Remove RabbitMQ

```bash
# RabbitMQ is no longer needed
docker stop servicedesk-rabbitmq
docker rm servicedesk-rabbitmq

# Remove RabbitMQ data volume
docker volume rm servicedesk_rabbitmq_data
```

### 4. Update Documentation

- Update runbooks with new deployment procedures
- Update monitoring alerts and thresholds
- Update incident response procedures
- Document new backup/restore procedures

### 5. Optimize Monolith

After migration, consider:

```bash
# Database optimization
psql -h localhost -U servicedesk -d servicedesk_monolith -c "VACUUM ANALYZE;"

# Update statistics
psql -h localhost -U servicedesk -d servicedesk_monolith -c "
  SELECT schemaname, tablename
  FROM pg_tables
  WHERE schemaname = 'public';" | while read schema table; do
    psql -h localhost -U servicedesk -d servicedesk_monolith -c "ANALYZE $table;"
done

# Review slow queries
psql -h localhost -U servicedesk -d servicedesk_monolith -c "
  SELECT query, calls, total_time, mean_time
  FROM pg_stat_statements
  ORDER BY mean_time DESC
  LIMIT 20;"
```

## Troubleshooting

### Issue: Monolith won't start

```bash
# Check logs
docker-compose -f docker-compose.monolith.yml logs servicedesk-monolith

# Common causes:
# 1. Database connection failed
# 2. Missing environment variables
# 3. Port 8080 already in use
# 4. Insufficient memory

# Check database connectivity
docker exec -it servicedesk-monolith sh
psql -h postgres -U servicedesk -d servicedesk_monolith
```

### Issue: Database migration failed

```bash
# Check Flyway migration status
docker exec -it servicedesk-postgres psql -U servicedesk -d servicedesk_monolith

# Query Flyway schema history
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

# If migration is stuck, you may need to repair
# docker-compose -f docker-compose.monolith.yml run servicedesk-monolith \
#   flyway repair

# Then retry
docker-compose -f docker-compose.monolith.yml restart servicedesk-monolith
```

### Issue: Performance degradation

```bash
# Check resource usage
docker stats servicedesk-monolith

# Check database connections
psql -h localhost -U servicedesk -d servicedesk_monolith -c "
  SELECT count(*) as connections, state
  FROM pg_stat_activity
  WHERE datname = 'servicedesk_monolith'
  GROUP BY state;"

# Check slow queries
psql -h localhost -U servicedesk -d servicedesk_monolith -c "
  SELECT pid, now() - query_start as duration, query
  FROM pg_stat_activity
  WHERE state = 'active' AND now() - query_start > interval '5 seconds'
  ORDER BY duration DESC;"

# Increase memory if needed (edit docker-compose.monolith.yml)
# services:
#   servicedesk-monolith:
#     deploy:
#       resources:
#         limits:
#           memory: 2G
#         reservations:
#           memory: 1.5G
```

### Issue: Missing data after migration

```bash
# Compare record counts
echo "Microservices databases:"
psql -h localhost -U servicedesk -d ticket_db -c "SELECT COUNT(*) FROM tickets;"

echo "Monolith database:"
psql -h localhost -U servicedesk -d servicedesk_monolith -c "SELECT COUNT(*) FROM tickets;"

# If counts don't match, re-import:
psql -h localhost -U servicedesk -d servicedesk_monolith < /backups/ticket_db.sql
```

### Issue: Authentication not working

```bash
# Check JWT secret is set
docker exec servicedesk-monolith env | grep JWT_SECRET

# Verify user records migrated
psql -h localhost -U servicedesk -d servicedesk_monolith -c "SELECT COUNT(*) FROM users;"

# Test authentication endpoint
curl -v -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@servicedesk.local", "password": "admin123"}'
```

## Getting Help

If you encounter issues during migration:

1. Check logs: `docker-compose -f docker-compose.monolith.yml logs -f`
2. Review this guide and [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
3. Search [GitHub Issues](https://github.com/your-org/servicedesk-platform/issues)
4. Ask in [GitHub Discussions](https://github.com/your-org/servicedesk-platform/discussions)
5. Contact support: support@greenwhite.uz

## Success Criteria

Migration is considered successful when:

- ✅ All services start without errors
- ✅ Health check returns `UP` status
- ✅ Authentication works correctly
- ✅ All functional tests pass
- ✅ Data integrity verified (record counts match)
- ✅ Performance meets SLAs
- ✅ Monitoring dashboards show green
- ✅ No critical errors in logs for 24 hours
- ✅ End-users can perform normal operations

Congratulations on successfully migrating to the monolithic architecture! 🎉
