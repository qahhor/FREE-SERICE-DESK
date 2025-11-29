# Deployment Guide - ServiceDesk Monolith

Complete deployment guide for the ServiceDesk monolithic application across different environments and platforms.

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Deployment Options](#deployment-options)
  - [Docker Compose (Recommended)](#docker-compose-recommended)
  - [Kubernetes](#kubernetes)
  - [Standalone JAR](#standalone-jar)
  - [Systemd Service](#systemd-service)
- [Environment Configuration](#environment-configuration)
- [Database Setup](#database-setup)
- [Scaling Strategies](#scaling-strategies)
- [High Availability](#high-availability)
- [Monitoring & Observability](#monitoring--observability)
- [Security Hardening](#security-hardening)
- [Backup & Disaster Recovery](#backup--disaster-recovery)
- [Troubleshooting](#troubleshooting)

## Overview

The ServiceDesk monolithic application is designed for easy deployment with multiple options:

- **Docker Compose**: Best for development and small-medium deployments
- **Kubernetes**: Best for large-scale, cloud-native deployments
- **Standalone JAR**: Best for traditional VM/bare-metal deployments
- **Systemd Service**: Best for Linux server deployments

## Quick Start

The fastest way to get ServiceDesk running:

```bash
# Clone the repository
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform

# Copy and configure environment
cp .env.example .env
nano .env  # Set JWT_SECRET (required)

# Start with Docker Compose
docker-compose -f docker-compose.monolith.yml up -d

# Access the application
open http://localhost:8080/swagger-ui.html
```

## Deployment Options

### Docker Compose (Recommended)

Best for: Development, staging, small production deployments (< 1000 users)

#### Prerequisites

- Docker 24.0+ and Docker Compose 2.20+
- 4GB RAM minimum (8GB recommended)
- 20GB disk space
- Linux, macOS, or Windows with WSL2

#### Production Deployment

```bash
# 1. Clone repository
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform

# 2. Create production environment file
cat > .env <<EOF
# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080

# Database
POSTGRES_PASSWORD=$(openssl rand -base64 32)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=servicedesk
DB_USERNAME=servicedesk
DB_PASSWORD=\${POSTGRES_PASSWORD}

# Security (CRITICAL: Change this!)
JWT_SECRET=$(openssl rand -base64 48)

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Elasticsearch
ELASTICSEARCH_URIS=http://elasticsearch:9200

# MinIO
MINIO_PASSWORD=$(openssl rand -base64 32)

# Email (Configure your SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@domain.com
MAIL_PASSWORD=your-app-password

# Optional: AI Features
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...

# Optional: Monitoring
GRAFANA_USER=admin
GRAFANA_PASSWORD=$(openssl rand -base64 24)
EOF

# 3. Build and start services
docker-compose -f docker-compose.monolith.yml up -d

# 4. Check status
docker-compose -f docker-compose.monolith.yml ps

# 5. View logs
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith

# 6. Verify health
curl http://localhost:8080/actuator/health
```

#### Docker Compose Commands

```bash
# Start services
docker-compose -f docker-compose.monolith.yml up -d

# Stop services
docker-compose -f docker-compose.monolith.yml stop

# Restart application only
docker-compose -f docker-compose.monolith.yml restart servicedesk-monolith

# View logs
docker-compose -f docker-compose.monolith.yml logs -f

# Scale application (if load balancer configured)
docker-compose -f docker-compose.monolith.yml up -d --scale servicedesk-monolith=3

# Update to new version
docker-compose -f docker-compose.monolith.yml pull
docker-compose -f docker-compose.monolith.yml up -d

# Clean up (removes containers and networks, keeps volumes)
docker-compose -f docker-compose.monolith.yml down

# Complete cleanup (WARNING: removes all data)
docker-compose -f docker-compose.monolith.yml down -v
```

### Kubernetes

Best for: Large production deployments (> 1000 users), multi-region, high availability

#### Prerequisites

- Kubernetes 1.28+
- kubectl configured
- Helm 3.x (optional, recommended)
- Persistent Volume provisioner
- Load balancer (cloud provider or metallb)

#### Architecture

```
┌─────────────────────────────────────────────────┐
│              Load Balancer / Ingress            │
└─────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
   ┌────▼────┐     ┌────▼────┐     ┌────▼────┐
   │  Pod 1  │     │  Pod 2  │     │  Pod 3  │
   │Monolith │     │Monolith │     │Monolith │
   └─────────┘     └─────────┘     └─────────┘
        │               │               │
        └───────────────┼───────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
   ┌────▼────┐     ┌────▼────┐     ┌────▼────┐
   │PostgreSQL│    │  Redis  │     │ElasticS│
   │(StatefulSet)  │(StatefulSet)  │(StatefulSet)
   └─────────┘     └─────────┘     └─────────┘
```

#### Deployment Steps

```bash
# 1. Create namespace
kubectl create namespace servicedesk

# 2. Create secrets
kubectl create secret generic servicedesk-secrets \
  --from-literal=jwt-secret=$(openssl rand -base64 48) \
  --from-literal=postgres-password=$(openssl rand -base64 32) \
  --from-literal=redis-password=$(openssl rand -base64 32) \
  --namespace servicedesk

# 3. Create ConfigMap
kubectl create configmap servicedesk-config \
  --from-file=application.yml=config/application-k8s.yml \
  --namespace servicedesk

# 4. Apply manifests
kubectl apply -f infrastructure/kubernetes/monolith/ -n servicedesk

# 5. Check deployment status
kubectl get pods -n servicedesk
kubectl get services -n servicedesk
kubectl get ingress -n servicedesk

# 6. View logs
kubectl logs -f deployment/servicedesk-monolith -n servicedesk

# 7. Check health
kubectl exec -it deployment/servicedesk-monolith -n servicedesk -- \
  curl http://localhost:8080/actuator/health
```

#### Kubernetes Manifests

**deployment.yaml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: servicedesk-monolith
  namespace: servicedesk
spec:
  replicas: 3
  selector:
    matchLabels:
      app: servicedesk-monolith
  template:
    metadata:
      labels:
        app: servicedesk-monolith
    spec:
      containers:
      - name: servicedesk
        image: your-registry/servicedesk-monolith:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DB_HOST
          value: "postgres-service"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: servicedesk-secrets
              key: postgres-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: servicedesk-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
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

**service.yaml**:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: servicedesk-monolith
  namespace: servicedesk
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    name: http
  selector:
    app: servicedesk-monolith
```

**ingress.yaml**:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: servicedesk-ingress
  namespace: servicedesk
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - servicedesk.yourdomain.com
    secretName: servicedesk-tls
  rules:
  - host: servicedesk.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: servicedesk-monolith
            port:
              number: 8080
```

#### Helm Chart (Optional)

```bash
# Install with Helm
helm install servicedesk ./infrastructure/helm/servicedesk \
  --namespace servicedesk \
  --create-namespace \
  --set image.tag=latest \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=servicedesk.yourdomain.com

# Upgrade
helm upgrade servicedesk ./infrastructure/helm/servicedesk \
  --namespace servicedesk \
  --set image.tag=v1.2.0

# Rollback
helm rollback servicedesk 1 --namespace servicedesk
```

### Standalone JAR

Best for: Traditional VM deployments, air-gapped environments, testing

#### Prerequisites

- Java 17+ (OpenJDK or Oracle JDK)
- PostgreSQL 16+
- Redis 7+
- Elasticsearch 8+ (optional but recommended)
- 2GB RAM minimum (4GB recommended)

#### Build from Source

```bash
# Clone repository
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform/backend

# Build with Maven
mvn clean package -pl monolith-app -am -DskipTests

# JAR location
ls -lh monolith-app/target/servicedesk-monolith.jar
```

#### Run Standalone

```bash
# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=servicedesk
export DB_USERNAME=servicedesk
export DB_PASSWORD=your_password
export JWT_SECRET=your-very-secure-jwt-secret-key-at-least-32-characters-long
export REDIS_HOST=localhost
export ELASTICSEARCH_URIS=http://localhost:9200

# Run application
java -Xms1g -Xmx2g \
  -Dspring.profiles.active=production \
  -Djava.security.egd=file:/dev/./urandom \
  -jar backend/monolith-app/target/servicedesk-monolith.jar

# Run in background
nohup java -Xms1g -Xmx2g \
  -Dspring.profiles.active=production \
  -jar backend/monolith-app/target/servicedesk-monolith.jar \
  > /var/log/servicedesk/application.log 2>&1 &

# Save PID
echo $! > /var/run/servicedesk.pid
```

#### External Configuration

Create `application-production.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/servicedesk
    username: servicedesk
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  data:
    redis:
      host: localhost
      port: 6379

  elasticsearch:
    uris: http://localhost:9200

server:
  port: 8080
  compression:
    enabled: true

logging:
  file:
    name: /var/log/servicedesk/application.log
  level:
    com.servicedesk: INFO
```

Run with external config:

```bash
java -jar servicedesk-monolith.jar \
  --spring.config.location=file:./application-production.yml
```

### Systemd Service

Best for: Linux production servers, long-running deployments

#### Create Service User

```bash
# Create dedicated user
sudo useradd -r -s /bin/false servicedesk

# Create directories
sudo mkdir -p /opt/servicedesk
sudo mkdir -p /var/log/servicedesk
sudo mkdir -p /etc/servicedesk

# Set permissions
sudo chown -R servicedesk:servicedesk /opt/servicedesk
sudo chown -R servicedesk:servicedesk /var/log/servicedesk
sudo chown -R servicedesk:servicedesk /etc/servicedesk
```

#### Install Application

```bash
# Copy JAR
sudo cp backend/monolith-app/target/servicedesk-monolith.jar /opt/servicedesk/

# Create configuration
sudo tee /etc/servicedesk/application.yml > /dev/null <<EOF
spring:
  profiles:
    active: production
  datasource:
    url: jdbc:postgresql://localhost:5432/servicedesk
    username: servicedesk
    password: \${DB_PASSWORD}
server:
  port: 8080
logging:
  file:
    name: /var/log/servicedesk/application.log
EOF

# Create environment file
sudo tee /etc/servicedesk/servicedesk.env > /dev/null <<EOF
DB_PASSWORD=your_secure_password
JWT_SECRET=your-very-secure-jwt-secret-key-at-least-32-characters-long
REDIS_HOST=localhost
ELASTICSEARCH_URIS=http://localhost:9200
EOF

# Secure environment file
sudo chmod 600 /etc/servicedesk/servicedesk.env
sudo chown servicedesk:servicedesk /etc/servicedesk/servicedesk.env
```

#### Create Systemd Service

```bash
sudo tee /etc/systemd/system/servicedesk.service > /dev/null <<EOF
[Unit]
Description=ServiceDesk Monolithic Application
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=servicedesk
Group=servicedesk

WorkingDirectory=/opt/servicedesk
EnvironmentFile=/etc/servicedesk/servicedesk.env

ExecStart=/usr/bin/java \\
    -Xms1g \\
    -Xmx2g \\
    -Djava.security.egd=file:/dev/./urandom \\
    -Dspring.config.location=file:/etc/servicedesk/application.yml \\
    -jar /opt/servicedesk/servicedesk-monolith.jar

# Restart policy
Restart=always
RestartSec=10

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/log/servicedesk

# Resource limits
LimitNOFILE=65536
TimeoutStartSec=120

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd
sudo systemctl daemon-reload

# Enable service
sudo systemctl enable servicedesk

# Start service
sudo systemctl start servicedesk

# Check status
sudo systemctl status servicedesk

# View logs
sudo journalctl -u servicedesk -f
```

#### Service Management

```bash
# Start
sudo systemctl start servicedesk

# Stop
sudo systemctl stop servicedesk

# Restart
sudo systemctl restart servicedesk

# Status
sudo systemctl status servicedesk

# Enable auto-start
sudo systemctl enable servicedesk

# Disable auto-start
sudo systemctl disable servicedesk

# View logs
sudo journalctl -u servicedesk -n 100 --no-pager

# Follow logs
sudo journalctl -u servicedesk -f
```

## Environment Configuration

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret (min 32 chars) | `your-secret-key-here` |
| `DB_PASSWORD` | Database password | `secureDbPass123` |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application port |
| `SPRING_PROFILES_ACTIVE` | `docker` | Active profile |
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | servicedesk | Database name |
| `DB_USERNAME` | servicedesk | Database username |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `ELASTICSEARCH_URIS` | http://localhost:9200 | Elasticsearch URL |
| `MAIL_HOST` | smtp.gmail.com | SMTP server |
| `MAIL_PORT` | 587 | SMTP port |
| `MAIL_USERNAME` | - | SMTP username |
| `MAIL_PASSWORD` | - | SMTP password |
| `OPENAI_API_KEY` | - | OpenAI API key |
| `ANTHROPIC_API_KEY` | - | Anthropic API key |
| `TELEGRAM_BOT_TOKEN` | - | Telegram bot token |
| `WHATSAPP_API_KEY` | - | WhatsApp API key |

### Configuration Profiles

| Profile | Purpose | File |
|---------|---------|------|
| `development` | Local development | application.yml |
| `docker` | Docker Compose | application-docker.yml |
| `kubernetes` | Kubernetes deployment | application-k8s.yml |
| `production` | Production settings | application-production.yml |

## Database Setup

### PostgreSQL Installation

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql-16 postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**CentOS/RHEL:**
```bash
sudo dnf install postgresql16-server
sudo postgresql-setup --initdb
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

**Docker:**
```bash
docker run -d \
  --name servicedesk-postgres \
  -e POSTGRES_DB=servicedesk \
  -e POSTGRES_USER=servicedesk \
  -e POSTGRES_PASSWORD=securepass \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:16-alpine
```

### Database Initialization

```bash
# Connect as postgres user
sudo -u postgres psql

# Create database and user
CREATE DATABASE servicedesk;
CREATE USER servicedesk WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE servicedesk TO servicedesk;

# Exit
\q
```

### Database Tuning

For production deployments, tune PostgreSQL:

```bash
# Edit postgresql.conf
sudo nano /etc/postgresql/16/main/postgresql.conf

# Recommended settings (adjust based on available RAM)
max_connections = 100
shared_buffers = 256MB          # 25% of RAM
effective_cache_size = 1GB      # 50-75% of RAM
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1          # For SSD, use 1.1; for HDD use 4
effective_io_concurrency = 200  # For SSD
work_mem = 2621kB
min_wal_size = 1GB
max_wal_size = 4GB

# Restart PostgreSQL
sudo systemctl restart postgresql
```

## Scaling Strategies

### Vertical Scaling

Increase resources for single instance:

**Docker Compose:**
```yaml
services:
  servicedesk-monolith:
    deploy:
      resources:
        limits:
          cpus: '4'
          memory: 8G
        reservations:
          cpus: '2'
          memory: 4G
```

**Kubernetes:**
```yaml
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "8Gi"
    cpu: "4000m"
```

### Horizontal Scaling

Run multiple instances with load balancer:

**Docker Compose with HAProxy:**
```yaml
services:
  servicedesk-monolith:
    image: your-registry/servicedesk-monolith:latest
    deploy:
      replicas: 3

  haproxy:
    image: haproxy:latest
    ports:
      - "80:80"
    volumes:
      - ./haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg
```

**Kubernetes:**
```bash
# Scale deployment
kubectl scale deployment servicedesk-monolith --replicas=5 -n servicedesk

# Auto-scaling
kubectl autoscale deployment servicedesk-monolith \
  --cpu-percent=70 \
  --min=3 \
  --max=10 \
  -n servicedesk
```

### Database Scaling

**Read Replicas:**
```yaml
spring:
  datasource:
    hikari:
      primary:
        jdbc-url: jdbc:postgresql://primary:5432/servicedesk
      replica:
        jdbc-url: jdbc:postgresql://replica:5432/servicedesk
```

**Connection Pooling:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## High Availability

### Database HA

**Patroni (PostgreSQL HA):**
```bash
# Install Patroni
sudo apt install patroni

# Configure cluster
# See infrastructure/patroni/patroni.yml
```

**pgBouncer (Connection Pooling):**
```bash
# Install pgBouncer
sudo apt install pgbouncer

# Configure
sudo nano /etc/pgbouncer/pgbouncer.ini
```

### Redis HA

**Redis Sentinel:**
```bash
docker run -d \
  --name redis-sentinel \
  -p 26379:26379 \
  redis:7-alpine redis-sentinel /etc/redis/sentinel.conf
```

**Redis Cluster:**
```bash
# Create 6-node cluster
docker-compose -f docker-compose.redis-cluster.yml up -d
```

### Application HA

**Load Balancer Health Checks:**
```yaml
# HAProxy health check
backend servicedesk
  option httpchk GET /actuator/health
  http-check expect status 200
  server app1 app1:8080 check inter 5s
  server app2 app2:8080 check inter 5s
  server app3 app3:8080 check inter 5s
```

## Monitoring & Observability

### Prometheus

```bash
# Access Prometheus
open http://localhost:9090

# Key metrics to monitor
- jvm_memory_used_bytes
- http_server_requests_seconds
- hikaricp_connections_active
- process_cpu_usage
```

### Grafana

```bash
# Access Grafana
open http://localhost:3000

# Import dashboard
# Dashboard ID: 12900 (Spring Boot Statistics)
```

### Application Logs

**Docker Compose:**
```bash
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith
```

**Kubernetes:**
```bash
kubectl logs -f deployment/servicedesk-monolith -n servicedesk
```

**Systemd:**
```bash
sudo journalctl -u servicedesk -f
```

### Alerts

Configure alerts in `infrastructure/prometheus/alerts.yml`:

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
      summary: "High JVM memory usage"
```

## Security Hardening

### SSL/TLS Configuration

**Nginx Reverse Proxy:**
```nginx
server {
    listen 443 ssl http2;
    server_name servicedesk.yourdomain.com;

    ssl_certificate /etc/ssl/certs/servicedesk.crt;
    ssl_certificate_key /etc/ssl/private/servicedesk.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Network Security

**Firewall Rules (UFW):**
```bash
# Allow SSH
sudo ufw allow 22/tcp

# Allow HTTPS
sudo ufw allow 443/tcp

# Allow HTTP (redirect to HTTPS)
sudo ufw allow 80/tcp

# Deny direct access to application
sudo ufw deny 8080/tcp

# Enable firewall
sudo ufw enable
```

### Secrets Management

**Kubernetes Secrets:**
```bash
# Create from file
kubectl create secret generic servicedesk-secrets \
  --from-file=jwt-secret=./jwt.secret \
  --from-file=db-password=./db.password \
  -n servicedesk

# Or use sealed-secrets
kubeseal --format yaml < secrets.yaml > sealed-secrets.yaml
kubectl apply -f sealed-secrets.yaml
```

**Docker Secrets:**
```yaml
services:
  servicedesk-monolith:
    secrets:
      - jwt_secret
      - db_password

secrets:
  jwt_secret:
    file: ./secrets/jwt.secret
  db_password:
    file: ./secrets/db.password
```

## Backup & Disaster Recovery

### Database Backup

**Automated Backup Script:**
```bash
#!/bin/bash
# /usr/local/bin/backup-servicedesk.sh

BACKUP_DIR="/backups/servicedesk"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup database
pg_dump -h localhost -U servicedesk -d servicedesk \
  | gzip > $BACKUP_DIR/servicedesk_$DATE.sql.gz

# Backup uploaded files (if using local storage)
tar -czf $BACKUP_DIR/files_$DATE.tar.gz /var/lib/servicedesk/uploads

# Remove old backups
find $BACKUP_DIR -name "*.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: $DATE"
```

**Cron Job:**
```bash
# Daily backup at 2 AM
0 2 * * * /usr/local/bin/backup-servicedesk.sh >> /var/log/servicedesk-backup.log 2>&1
```

### Restore Procedure

```bash
# Stop application
docker-compose -f docker-compose.monolith.yml stop servicedesk-monolith

# Restore database
gunzip < /backups/servicedesk/servicedesk_20240115_020000.sql.gz | \
  psql -h localhost -U servicedesk -d servicedesk

# Restore files
tar -xzf /backups/servicedesk/files_20240115_020000.tar.gz -C /

# Start application
docker-compose -f docker-compose.monolith.yml start servicedesk-monolith
```

## Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose -f docker-compose.monolith.yml logs servicedesk-monolith

# Common issues:
# 1. Database not ready - wait for PostgreSQL to be healthy
# 2. Missing JWT_SECRET - set in .env file
# 3. Port 8080 in use - change SERVER_PORT
# 4. Insufficient memory - increase Docker memory limit
```

### High Memory Usage

```bash
# Check memory usage
docker stats servicedesk-monolith

# Tune JVM settings
java -Xms1g -Xmx2g -XX:+UseG1GC -jar servicedesk-monolith.jar
```

### Slow Database Queries

```bash
# Enable slow query log
ALTER DATABASE servicedesk SET log_min_duration_statement = 1000;

# View slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### Connection Pool Exhausted

```bash
# Increase pool size
spring:
  datasource:
    hikari:
      maximum-pool-size: 30  # Increase from default 20
```

## Getting Help

- **Documentation**: [README.md](README.md), [ARCHITECTURE.md](ARCHITECTURE.md)
- **Issues**: [GitHub Issues](https://github.com/your-org/servicedesk-platform/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/servicedesk-platform/discussions)
- **Support**: support@greenwhite.uz

---

**Deployed with ❤️ by [Green White Solutions](https://greenwhite.uz)**
