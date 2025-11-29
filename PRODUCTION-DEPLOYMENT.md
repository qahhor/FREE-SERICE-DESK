# Production Deployment Guide

Complete guide for deploying ServiceDesk Monolith to production environment.

## 📋 Table of Contents

- [Pre-Deployment Checklist](#pre-deployment-checklist)
- [System Requirements](#system-requirements)
- [Build Production Artifact](#build-production-artifact)
- [Deployment Options](#deployment-options)
  - [Docker Compose](#option-1-docker-compose-recommended)
  - [Kubernetes](#option-2-kubernetes)
  - [Standalone JAR](#option-3-standalone-jar)
- [Security Configuration](#security-configuration)
- [Database Setup](#database-setup)
- [SSL/TLS Configuration](#ssltls-configuration)
- [Monitoring Setup](#monitoring-setup)
- [Backup Configuration](#backup-configuration)
- [Post-Deployment Verification](#post-deployment-verification)
- [Troubleshooting](#troubleshooting)

## Pre-Deployment Checklist

Before deploying to production, ensure you have:

### Infrastructure
- [ ] Production server(s) provisioned (min 4GB RAM, 2 CPU cores)
- [ ] Domain name configured and DNS records updated
- [ ] SSL/TLS certificates obtained (Let's Encrypt or commercial)
- [ ] Firewall rules configured
- [ ] Backup solution in place
- [ ] Monitoring solution setup (Prometheus/Grafana)

### Software Prerequisites
- [ ] Docker 24+ and Docker Compose 2.20+ (for Docker deployment)
- [ ] OR Java 17+ and PostgreSQL 16+ (for JAR deployment)
- [ ] Redis 7+ installed
- [ ] Elasticsearch 8+ installed (optional but recommended)
- [ ] Reverse proxy (nginx/Apache) configured

### Configuration
- [ ] Production `.env` file prepared with all secrets
- [ ] SMTP credentials configured and tested
- [ ] Database backup strategy defined
- [ ] Log rotation configured
- [ ] Resource limits defined

### Security
- [ ] Strong JWT_SECRET generated (min 48 characters)
- [ ] Database passwords changed from defaults
- [ ] All API keys and tokens secured
- [ ] Rate limiting configured
- [ ] CORS origins properly set

## System Requirements

### Minimum Requirements (< 100 users)
- **CPU**: 2 cores
- **RAM**: 4GB
- **Disk**: 50GB SSD
- **Network**: 100 Mbps

### Recommended Requirements (100-1000 users)
- **CPU**: 4 cores
- **RAM**: 8GB
- **Disk**: 100GB SSD
- **Network**: 1 Gbps

### High-Load Requirements (1000+ users)
- **CPU**: 8+ cores
- **RAM**: 16GB+
- **Disk**: 200GB+ SSD (NVMe preferred)
- **Network**: 1 Gbps+
- **Load Balancer**: Yes
- **Multiple instances**: Yes (3+)

## Build Production Artifact

### Step 1: Clone Repository

```bash
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform
git checkout main  # or your production branch
```

### Step 2: Build Artifact

```bash
# Run build script
./scripts/build-production.sh

# This creates:
# - build/artifacts/servicedesk-monolith-1.0.0.jar
# - build/config/application-production.yml
# - build/docker/Dockerfile
# - build/docker/docker-compose.monolith.yml
# - build/BUILD_INFO.txt
# - build/QUICK_START.md
```

### Step 3: Verify Build

```bash
# Check build output
ls -lh build/artifacts/

# Verify JAR
java -jar build/artifacts/servicedesk-monolith-latest.jar --version

# Review build info
cat build/BUILD_INFO.txt
```

## Deployment Options

## Option 1: Docker Compose (Recommended)

Best for: Small to medium production deployments, single-server setup

### Setup Steps

```bash
# 1. Create deployment directory
sudo mkdir -p /opt/servicedesk/production
cd /opt/servicedesk/production

# 2. Copy build artifacts
cp -r /path/to/build/* .

# 3. Create production environment file
cp .env.production.example .env

# 4. Configure environment variables
nano .env
```

**Critical variables to set:**

```bash
# Generate strong secrets
JWT_SECRET=$(openssl rand -base64 48)
DB_PASSWORD=$(openssl rand -base64 32)
MINIO_SECRET_KEY=$(openssl rand -base64 32)
GRAFANA_ADMIN_PASSWORD=$(openssl rand -base64 24)

# Update .env with these values
```

### Deploy with Docker Compose

```bash
# 5. Start services
cd docker/
docker-compose -f docker-compose.monolith.yml up -d

# 6. Monitor startup
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith

# Wait for: "Started ServiceDeskMonolithApplication"

# 7. Verify health
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

### Configure Nginx Reverse Proxy

```nginx
# /etc/nginx/sites-available/servicedesk

upstream servicedesk_backend {
    server localhost:8080;
    # For multiple instances:
    # server localhost:8081;
    # server localhost:8082;
}

server {
    listen 80;
    server_name servicedesk.yourdomain.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name servicedesk.yourdomain.com;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/servicedesk.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/servicedesk.yourdomain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # File upload limit
    client_max_body_size 50M;

    # Compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    location / {
        proxy_pass http://servicedesk_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Health check endpoint (no auth required)
    location /actuator/health {
        proxy_pass http://servicedesk_backend;
        access_log off;
    }
}
```

Enable and test:

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/servicedesk /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx

# Verify HTTPS
curl https://servicedesk.yourdomain.com/actuator/health
```

## Option 2: Kubernetes

Best for: Large-scale deployments, multi-region, high availability

### Prerequisites

```bash
# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install Helm (optional)
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### Deploy to Kubernetes

```bash
# 1. Create namespace
kubectl create namespace servicedesk

# 2. Create secrets
kubectl create secret generic servicedesk-secrets \
  --from-literal=jwt-secret=$(openssl rand -base64 48) \
  --from-literal=db-password=$(openssl rand -base64 32) \
  --from-literal=redis-password=$(openssl rand -base64 32) \
  --namespace servicedesk

# 3. Create ConfigMap
kubectl create configmap servicedesk-config \
  --from-file=application.yml=config/application-production.yml \
  --namespace servicedesk

# 4. Build and push Docker image
docker build -t your-registry/servicedesk-monolith:1.0.0 -f docker/Dockerfile .
docker push your-registry/servicedesk-monolith:1.0.0

# 5. Apply Kubernetes manifests
kubectl apply -f infrastructure/kubernetes/monolith/ -n servicedesk

# 6. Check deployment
kubectl get pods -n servicedesk
kubectl get services -n servicedesk
kubectl get ingress -n servicedesk

# 7. View logs
kubectl logs -f deployment/servicedesk-monolith -n servicedesk
```

### Kubernetes Health Checks

The application provides:
- **Liveness Probe**: `/actuator/health/liveness`
- **Readiness Probe**: `/actuator/health/readiness`

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

## Option 3: Standalone JAR

Best for: Traditional server deployments, VM-based hosting

### Setup Steps

```bash
# 1. Create service user
sudo useradd -r -s /bin/false -d /opt/servicedesk servicedesk

# 2. Create directories
sudo mkdir -p /opt/servicedesk/{bin,config,logs,data}
sudo mkdir -p /var/log/servicedesk

# 3. Copy artifacts
sudo cp build/artifacts/servicedesk-monolith-latest.jar /opt/servicedesk/bin/
sudo cp build/config/application-production.yml /opt/servicedesk/config/

# 4. Create environment file
sudo tee /opt/servicedesk/config/servicedesk.env > /dev/null <<'EOF'
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=servicedesk
DB_USERNAME=servicedesk
DB_PASSWORD=CHANGE_ME

# Security
JWT_SECRET=CHANGE_ME_TO_SECURE_VALUE

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Elasticsearch
ELASTICSEARCH_URIS=http://localhost:9200

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@yourdomain.com
MAIL_PASSWORD=CHANGE_ME

# Storage
STORAGE_TYPE=local
FILE_UPLOAD_DIR=/opt/servicedesk/data/uploads
EOF

# 5. Secure environment file
sudo chmod 600 /opt/servicedesk/config/servicedesk.env

# 6. Set ownership
sudo chown -R servicedesk:servicedesk /opt/servicedesk
sudo chown -R servicedesk:servicedesk /var/log/servicedesk

# 7. Create systemd service
sudo tee /etc/systemd/system/servicedesk.service > /dev/null <<'EOF'
[Unit]
Description=ServiceDesk Monolithic Application
Documentation=https://github.com/your-org/servicedesk-platform
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=servicedesk
Group=servicedesk
WorkingDirectory=/opt/servicedesk

# Environment
EnvironmentFile=/opt/servicedesk/config/servicedesk.env

# JVM Options
Environment="JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# Application
ExecStart=/usr/bin/java ${JAVA_OPTS} \
    -Dspring.config.location=file:/opt/servicedesk/config/application-production.yml \
    -Dlogging.file.name=/var/log/servicedesk/application.log \
    -jar /opt/servicedesk/bin/servicedesk-monolith-latest.jar

# Restart policy
Restart=always
RestartSec=10
StartLimitBurst=3
StartLimitInterval=300

# Security
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/log/servicedesk /opt/servicedesk/data

# Resource Limits
LimitNOFILE=65536
LimitNPROC=4096
TimeoutStartSec=120
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
EOF

# 8. Reload systemd
sudo systemctl daemon-reload

# 9. Enable and start service
sudo systemctl enable servicedesk
sudo systemctl start servicedesk

# 10. Check status
sudo systemctl status servicedesk

# 11. View logs
sudo journalctl -u servicedesk -f
```

## Security Configuration

### 1. Generate Secure Secrets

```bash
# JWT Secret (min 48 characters)
openssl rand -base64 48

# Database Password
openssl rand -base64 32

# Redis Password (if enabled)
openssl rand -base64 32

# Store securely in password manager!
```

### 2. Firewall Configuration

```bash
# Ubuntu/Debian (UFW)
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw deny 8080/tcp   # Block direct access to app
sudo ufw enable

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 3. Database Security

```sql
-- Create dedicated database user with limited privileges
CREATE USER servicedesk WITH PASSWORD 'secure_password';
CREATE DATABASE servicedesk OWNER servicedesk;

-- Grant only necessary privileges
GRANT CONNECT ON DATABASE servicedesk TO servicedesk;
GRANT USAGE ON SCHEMA public TO servicedesk;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO servicedesk;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO servicedesk;

-- Enable SSL (in postgresql.conf)
ssl = on
ssl_cert_file = '/path/to/server.crt'
ssl_key_file = '/path/to/server.key'
```

### 4. Application Security Headers

Already configured in nginx example above. Key headers:
- `Strict-Transport-Security`
- `X-Frame-Options`
- `X-Content-Type-Options`
- `X-XSS-Protection`

## Database Setup

### PostgreSQL Installation and Configuration

```bash
# Install PostgreSQL 16
sudo apt update
sudo apt install postgresql-16 postgresql-contrib-16

# Start and enable
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create database and user
sudo -u postgres psql <<EOF
CREATE DATABASE servicedesk;
CREATE USER servicedesk WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE servicedesk TO servicedesk;
\q
EOF
```

### Database Tuning

Edit `/etc/postgresql/16/main/postgresql.conf`:

```ini
# Memory Settings (adjust based on available RAM)
shared_buffers = 256MB              # 25% of system RAM
effective_cache_size = 1GB          # 50-75% of system RAM
maintenance_work_mem = 64MB
work_mem = 2621kB

# Checkpoint Settings
checkpoint_completion_target = 0.9
wal_buffers = 16MB
min_wal_size = 1GB
max_wal_size = 4GB

# Connection Settings
max_connections = 100

# SSD Optimization
random_page_cost = 1.1              # Use 4.0 for HDD
effective_io_concurrency = 200      # Use 2 for HDD

# Query Planner
default_statistics_target = 100
```

Restart PostgreSQL:

```bash
sudo systemctl restart postgresql
```

## SSL/TLS Configuration

### Option 1: Let's Encrypt (Free)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d servicedesk.yourdomain.com

# Auto-renewal is configured automatically
# Test renewal
sudo certbot renew --dry-run
```

### Option 2: Commercial Certificate

```bash
# Copy certificates
sudo cp yourdomain.crt /etc/ssl/certs/servicedesk.crt
sudo cp yourdomain.key /etc/ssl/private/servicedesk.key

# Set permissions
sudo chmod 644 /etc/ssl/certs/servicedesk.crt
sudo chmod 600 /etc/ssl/private/servicedesk.key

# Update nginx configuration with certificate paths
```

## Monitoring Setup

### Prometheus + Grafana

Already included in docker-compose.monolith.yml:

```bash
# Access Prometheus
open http://your-server:9090

# Access Grafana
open http://your-server:3000
# Login: admin / (password from .env)

# Import ServiceDesk dashboard
# Dashboard JSON: infrastructure/grafana/dashboards/servicedesk-monolith.json
```

### Key Metrics to Monitor

- **Application**:
  - JVM memory usage
  - HTTP request rate
  - HTTP error rate (4xx, 5xx)
  - Response time (p50, p95, p99)

- **Database**:
  - Active connections
  - Query duration
  - Lock waits

- **System**:
  - CPU usage
  - Memory usage
  - Disk I/O
  - Network traffic

### Set Up Alerts

Edit `infrastructure/prometheus/alerts.yml`:

```yaml
groups:
- name: servicedesk
  rules:
  - alert: HighMemoryUsage
    expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) > 0.9
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High JVM memory usage on {{ $labels.instance }}"
      description: "JVM memory usage is above 90%"

  - alert: HighErrorRate
    expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate on {{ $labels.instance }}"
      description: "Error rate is {{ $value }} per second"

  - alert: ApplicationDown
    expr: up{job="servicedesk-monolith"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "ServiceDesk application is down"
      description: "Application {{ $labels.instance }} is not responding"
```

## Backup Configuration

### Automated Database Backup

```bash
# Create backup script
sudo tee /usr/local/bin/backup-servicedesk.sh > /dev/null <<'EOF'
#!/bin/bash
set -e

BACKUP_DIR="/backups/servicedesk"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

mkdir -p $BACKUP_DIR

# Backup database
pg_dump -h localhost -U servicedesk -d servicedesk | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# Backup uploads (if using local storage)
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /opt/servicedesk/data/uploads 2>/dev/null || true

# Remove old backups
find $BACKUP_DIR -name "*.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: $DATE"
EOF

sudo chmod +x /usr/local/bin/backup-servicedesk.sh

# Create cron job (daily at 2 AM)
sudo crontab -e
# Add: 0 2 * * * /usr/local/bin/backup-servicedesk.sh >> /var/log/servicedesk-backup.log 2>&1
```

### Backup to S3 (Optional)

```bash
# Install AWS CLI
sudo apt install awscli

# Configure AWS credentials
aws configure

# Update backup script to sync to S3
aws s3 sync $BACKUP_DIR s3://your-backup-bucket/servicedesk/
```

## Post-Deployment Verification

### 1. Health Checks

```bash
# Application health
curl https://servicedesk.yourdomain.com/actuator/health

# Expected: {"status":"UP","components":{"db":{"status":"UP"},...}}

# Application info
curl https://servicedesk.yourdomain.com/actuator/info

# Metrics
curl https://servicedesk.yourdomain.com/actuator/metrics
```

### 2. Functional Tests

```bash
# Test authentication
curl -X POST https://servicedesk.yourdomain.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@servicedesk.local","password":"admin123"}'

# Should return JWT token

# Test API with token
curl https://servicedesk.yourdomain.com/api/v1/tickets \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Load Testing (Optional)

```bash
# Install Apache Bench
sudo apt install apache2-utils

# Run load test
ab -n 1000 -c 10 https://servicedesk.yourdomain.com/actuator/health

# Expected: No errors, reasonable response times
```

### 4. Security Audit

```bash
# SSL/TLS test
curl -I https://servicedesk.yourdomain.com

# Check for security headers
# Should see: Strict-Transport-Security, X-Frame-Options, etc.

# Port scan (from external host)
nmap -sV your-server-ip

# Expected: Only 22, 80, 443 open
```

## Troubleshooting

### Application Won't Start

```bash
# Check logs
sudo journalctl -u servicedesk -n 100 --no-pager

# Common issues:
# 1. Database connection failed
# 2. Port 8080 in use
# 3. Missing JWT_SECRET
# 4. Insufficient memory

# Check database connectivity
psql -h localhost -U servicedesk -d servicedesk -c "SELECT 1"

# Check port usage
sudo lsof -i :8080
```

### High Memory Usage

```bash
# Check JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Increase heap size in systemd service
# Environment="JAVA_OPTS=-Xms2g -Xmx4g"

# Restart service
sudo systemctl restart servicedesk
```

### Database Connection Pool Exhausted

```bash
# Check active connections
psql -h localhost -U postgres -c "
  SELECT count(*), state
  FROM pg_stat_activity
  WHERE datname = 'servicedesk'
  GROUP BY state;
"

# Increase pool size in application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
```

### Slow Performance

```bash
# Enable slow query log in PostgreSQL
ALTER DATABASE servicedesk SET log_min_duration_statement = 1000;

# Check slow queries
psql -h localhost -U servicedesk -d servicedesk -c "
  SELECT query, calls, total_time, mean_time
  FROM pg_stat_statements
  ORDER BY mean_time DESC
  LIMIT 10;
"

# Analyze table statistics
psql -h localhost -U servicedesk -d servicedesk -c "VACUUM ANALYZE;"
```

## Support & Resources

- **Documentation**: [README.md](README.md), [ARCHITECTURE.md](ARCHITECTURE.md), [DEPLOYMENT.md](DEPLOYMENT.md)
- **Issues**: [GitHub Issues](https://github.com/your-org/servicedesk-platform/issues)
- **Support**: support@greenwhite.uz
- **Community**: [GitHub Discussions](https://github.com/your-org/servicedesk-platform/discussions)

---

**Deployed successfully? Let us know at support@greenwhite.uz** 🎉
