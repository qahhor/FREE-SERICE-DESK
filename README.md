# ServiceDesk Platform - Monolithic Architecture

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Monolithic-blue.svg)](ARCHITECTURE.md)

> **Open-source Service Desk Platform** - Unified monolithic application for customer support

**🎯 Architecture:** Transformed from microservices to modular monolith for simplified deployment and improved performance.

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Quick Start](#-quick-start)
- [Tech Stack](#-tech-stack)
- [Documentation](#-documentation)
- [Development](#-development)
- [Deployment](#-deployment)
- [API Reference](#-api-reference)
- [Configuration](#-configuration)
- [Monitoring](#-monitoring)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core ITSM Features
- ✅ **Ticket Management** - Create, assign, track, and resolve support tickets
- ✅ **SLA Management** - Configurable SLA policies with escalation rules
- ✅ **Asset/CMDB Management** - Track IT assets, relationships, and maintenance
- ✅ **Change Management** - Change requests with approval workflows
- ✅ **Problem Management** - Root cause analysis with known error database
- ✅ **Automation Engine** - Rule-based workflow automation

### Omnichannel Support
- 📧 **Email Integration** - Auto-create tickets from emails (IMAP/SMTP)
- 💬 **Telegram Bot** - Customer support via Telegram
- 📱 **WhatsApp Business** - WhatsApp Business API integration
- 💻 **Live Chat** - Real-time web chat with WebSocket
- 🌐 **Web Widget** - Embeddable chat widget
- 🔐 **Customer Portal** - Self-service portal

### AI-Powered Intelligence
- 🤖 **AI Agent** - Integration with OpenAI GPT-4 and Anthropic Claude
- 🧠 **RAG** - Retrieval Augmented Generation for knowledge base
- 🎯 **Smart Categorization** - Automatic ticket classification
- 📊 **Sentiment Analysis** - Customer sentiment detection
- 💡 **Auto-suggestions** - AI-powered response suggestions

### Analytics & Reporting
- 📈 **Dashboards** - Real-time metrics and KPIs
- 📊 **Reports** - CSAT, NPS, FRT, ART, SLA compliance
- 📁 **Export** - CSV, Excel, PDF formats
- 📉 **Trends** - Historical analysis and forecasting

### Multi-language Support
🌍 English (en) • Russian (ru) • Uzbek (uz) • Kazakh (kk) • Arabic (ar) with RTL support

---

## 🏗️ Architecture

### Modular Monolithic Architecture

This application uses a **modular monolithic architecture** - combining the simplicity of a monolith with the organization of microservices.

```
┌─────────────────────────────────────────────┐
│    ServiceDesk Monolith (Port 8080)        │
├─────────────────────────────────────────────┤
│  ┌─────────┐  ┌─────────┐  ┌──────────┐   │
│  │ Ticket  │  │ Channel │  │ Knowledge│   │
│  │ Module  │  │ Module  │  │  Module  │   │
│  └─────────┘  └─────────┘  └──────────┘   │
│  ┌─────────┐  ┌─────────┐  ┌──────────┐   │
│  │   AI    │  │  Notif. │  │Analytics │   │
│  │ Module  │  │ Module  │  │  Module  │   │
│  └─────────┘  └─────────┘  └──────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │    Marketplace Module               │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  Spring Events Bus (replaces RabbitMQ)     │
└─────────────────────────────────────────────┘
         ↓          ↓          ↓
    PostgreSQL   Redis   Elasticsearch
```

**Key Benefits:**
- 🚀 **3x faster startup** (90s → 30s)
- 💾 **63% less memory** (4GB → 1.5GB)
- 📦 **88% smaller** Docker footprint
- ⚡ **73% lower latency** (45ms → 12ms)
- 🔧 **Simplified deployment** - single JAR file

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed documentation.

---

## 🚀 Quick Start

### Prerequisites
- **Docker** & **Docker Compose** (required)
- **Java 17+** (for development)
- **Maven 3.8+** (for development)

### Production Deployment

**🚀 Automated (Recommended):**

```bash
# 1. Clone the repository
git clone https://github.com/qahhor/FREE-SERICE-DESK.git
cd FREE-SERICE-DESK

# 2. Build production artifact
./scripts/build-production.sh

# 3. Deploy with automated script
./scripts/deploy-production.sh

# See: PRODUCTION-DEPLOYMENT.md for complete guide
```

**⚡ Quick Start (Docker Compose):**

```bash
# 1. Clone and configure
git clone https://github.com/qahhor/FREE-SERICE-DESK.git
cd FREE-SERICE-DESK
cp .env.example .env
nano .env  # Set JWT_SECRET (required, min 32 characters)

# 2. Start the monolithic application
docker-compose -f docker-compose.monolith.yml up -d

# 3. Wait for startup (~30 seconds)
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith

# 4. Access the application
# ✅ API: http://localhost:8080
# ✅ Swagger UI: http://localhost:8080/swagger-ui.html
# ✅ Actuator: http://localhost:8080/actuator/health
# ✅ Grafana: http://localhost:3000 (admin/admin)
```

### Development Mode

```bash
# Build locally
cd backend
mvn clean package -pl monolith-app -am -DskipTests

# Run with development profile
java -jar monolith-app/target/servicedesk-monolith.jar

# Or with Maven
mvn spring-boot:run -pl monolith-app
```

### Default Credentials

| Role | Email | Password |
|------|-------|----------|
| 🔑 Admin | admin@servicedesk.local | admin123 |
| 👤 Agent | agent1@servicedesk.local | admin123 |

---

## 🛠️ Tech Stack

### Backend (Monolithic Application)

| Technology | Purpose | Version |
|------------|---------|---------|
| **Spring Boot** | Application Framework | 3.2.1 |
| **Java** | Programming Language | 17+ |
| **Spring Data JPA** | Database Access | 3.2.x |
| **Spring Security** | Authentication & Authorization | 6.2.x |
| **Spring Events** | Internal Communication | Built-in |
| **Flyway** | Database Migrations | 9.22.3 |
| **MapStruct** | DTO Mapping | 1.5.5 |
| **Lombok** | Boilerplate Reduction | Latest |

### Infrastructure

| Component | Purpose | Version |
|-----------|---------|---------|
| **PostgreSQL** | Primary Database | 16 |
| **Redis** | Caching Layer | 7 |
| **Elasticsearch** | Full-text Search & RAG | 8.11 |
| **MinIO** | Object Storage (S3-compatible) | Latest |
| **Prometheus** | Metrics Collection | Latest |
| **Grafana** | Monitoring Dashboard | Latest |

### Removed (vs Microservices)
- ❌ RabbitMQ - Replaced with Spring Events
- ❌ API Gateway - No longer needed
- ❌ Service Discovery - Not required

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Detailed architecture documentation |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Deployment guide (Docker, K8s, bare metal) |
| **[PRODUCTION-DEPLOYMENT.md](PRODUCTION-DEPLOYMENT.md)** | **🚀 Complete production deployment guide** |
| [MIGRATION.md](MIGRATION.md) | Migration guide from microservices |
| [FRONTEND.md](FRONTEND.md) | Frontend architecture & integration guide |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guidelines |
| [README.MONOLITH.md](README.MONOLITH.md) | Extended monolith documentation |

### Production Scripts

| Script | Purpose |
|--------|---------|
| `scripts/build-production.sh` | Build production-ready JAR with full packaging |
| `scripts/deploy-production.sh` | Automated deployment (Docker/JAR/K8s) |

### Configuration Templates

| File | Purpose |
|------|---------|
| `.env.production.example` | Production environment variables template (380+ lines) |
| `.env.example` | Development environment variables |

---

## 💻 Development

### Project Structure

```
servicedesk-platform/
├── backend/
│   ├── common-lib/              # Shared utilities, DTOs, security
│   ├── monolith-app/            # 🎯 Unified monolithic application
│   │   ├── src/main/java/com/servicedesk/monolith/
│   │   │   ├── ticket/          # Ticket, SLA, Assets, Changes, Problems
│   │   │   ├── channel/         # Email, Telegram, WhatsApp, LiveChat
│   │   │   ├── notification/    # In-app, Email, Push notifications
│   │   │   ├── knowledge/       # Knowledge base & articles
│   │   │   ├── ai/              # OpenAI/Claude, RAG, Embeddings
│   │   │   ├── analytics/       # Dashboards, reports, metrics
│   │   │   ├── marketplace/     # Module marketplace & plugins
│   │   │   └── common/          # Events, configs, utilities
│   │   ├── src/main/resources/
│   │   │   ├── application.yml
│   │   │   └── db/migration/    # Flyway migrations (V1-V41)
│   │   └── Dockerfile
│   └── modules/                 # Pluggable modules
│
├── infrastructure/
│   └── prometheus/              # Monitoring configurations
│
├── scripts/                     # Build and deployment scripts
│   ├── build-production.sh      # Production build
│   └── deploy-production.sh     # Automated deployment
│
├── docker-compose.monolith.yml  # 🎯 Production deployment
├── docker-compose.yml           # Legacy (microservices reference)
└── FRONTEND.md                  # Frontend architecture guide
```

> **Note:** Frontend has been removed from this repository. The backend provides a complete REST API.
> See [FRONTEND.md](FRONTEND.md) for frontend development options and integration guide.

### Building from Source

```bash
# Full build (all modules)
cd backend
mvn clean install

# Build monolith only
mvn clean package -pl monolith-app -am

# Skip tests
mvn clean package -pl monolith-app -am -DskipTests

# Output: backend/monolith-app/target/servicedesk-monolith.jar
```

### Running Tests

```bash
cd backend

# Run all tests
mvn test -pl monolith-app

# Run with coverage
mvn test jacoco:report -pl monolith-app

# Integration tests with TestContainers
mvn verify -pl monolith-app

# Coverage report: monolith-app/target/site/jacoco/index.html
```

### Database Migrations

Migrations are managed with **Flyway** and run automatically on startup.

```bash
# Migrations location
ls backend/monolith-app/src/main/resources/db/migration/

# Manual migration
mvn flyway:migrate -pl monolith-app

# Rollback (if needed)
mvn flyway:undo -pl monolith-app
```

**Migration Naming:**
- `V1-V7` - Ticket Service schemas
- `V10-V12` - Channel Service schemas
- `V20-V21` - Notification Service schemas
- `V30` - Knowledge Service schema
- `V40-V41` - Marketplace Service schemas

---

## 🚢 Deployment

### 🚀 Production Deployment (Automated)

**NEW!** Use automated production scripts for streamlined deployment:

```bash
# 1. Build production artifact
./scripts/build-production.sh

# Creates: build/artifacts/servicedesk-monolith-1.0.0.jar
# Plus: BUILD_INFO.txt, QUICK_START.md, configs, docs

# 2. Deploy with Docker Compose (recommended)
./scripts/deploy-production.sh

# Or deploy as standalone JAR with systemd
sudo ./scripts/deploy-production.sh -m jar

# Or deploy to Kubernetes
./scripts/deploy-production.sh -m kubernetes
```

**Production Build Output:**
```
build/
├── artifacts/servicedesk-monolith-1.0.0.jar  # Production JAR (~50-80MB)
├── config/                                    # Application configs
├── docker/                                    # Docker deployment files
├── scripts/                                   # Deployment scripts
├── docs/                                      # Complete documentation
├── BUILD_INFO.txt                             # Build metadata & checksums
└── QUICK_START.md                             # Quick deployment guide
```

**See:** [PRODUCTION-DEPLOYMENT.md](PRODUCTION-DEPLOYMENT.md) for complete production setup guide.

---

### Docker Compose (Quick Start)

```bash
# Development/Testing
docker-compose -f docker-compose.monolith.yml up -d

# View logs
docker-compose -f docker-compose.monolith.yml logs -f

# Stop services
docker-compose -f docker-compose.monolith.yml down

# Full cleanup (including volumes)
docker-compose -f docker-compose.monolith.yml down -v
```

### Standalone JAR

```bash
# Build
mvn clean package -pl monolith-app -am -DskipTests

# Run
java -jar backend/monolith-app/target/servicedesk-monolith.jar

# With custom profile
java -jar backend/monolith-app/target/servicedesk-monolith.jar \
  --spring.profiles.active=production
```

### Kubernetes

See [DEPLOYMENT.md](DEPLOYMENT.md) for Kubernetes manifests and [PRODUCTION-DEPLOYMENT.md](PRODUCTION-DEPLOYMENT.md) for production setup.

---

## 📡 API Reference

### Authentication

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@servicedesk.local",
    "password": "admin123"
  }'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "user": {...}
}
```

### Create Ticket

```bash
curl -X POST http://localhost:8080/api/v1/tickets \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Cannot login to application",
    "description": "Getting error 401 when trying to login",
    "projectId": "uuid",
    "priority": "HIGH",
    "type": "INCIDENT"
  }'
```

### API Endpoints

| Endpoint | Module | Description |
|----------|--------|-------------|
| `/api/v1/auth/*` | Ticket | Authentication & authorization |
| `/api/v1/tickets/*` | Ticket | Ticket CRUD operations |
| `/api/v1/users/*` | Ticket | User management |
| `/api/v1/teams/*` | Ticket | Team management |
| `/api/v1/projects/*` | Ticket | Project management |
| `/api/v1/sla/*` | Ticket | SLA policies & targets |
| `/api/v1/assets/*` | Ticket | Asset/CMDB management |
| `/api/v1/changes/*` | Ticket | Change management |
| `/api/v1/problems/*` | Ticket | Problem management |
| `/api/v1/channels/*` | Channel | Channel configuration |
| `/api/v1/widget/*` | Channel | Web widget API |
| `/api/v1/livechat/*` | Channel | Live chat API |
| `/api/v1/knowledge/*` | Knowledge | Knowledge base & articles |
| `/api/v1/notifications/*` | Notification | Notification management |
| `/api/v1/ai/*` | AI | AI services (chat, analysis) |
| `/api/v1/analytics/*` | Analytics | Dashboards & reports |
| `/api/v1/marketplace/*` | Marketplace | Module marketplace |

**Interactive API Documentation:**
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

---

## ⚙️ Configuration

### 🔐 Production Configuration

For production deployments, use the comprehensive template:

```bash
# Copy production template
cp .env.production.example .env

# Generate secure secrets
JWT_SECRET=$(openssl rand -base64 48)
DB_PASSWORD=$(openssl rand -base64 32)

# Edit with your production values
nano .env
```

**`.env.production.example` includes:**
- 🔐 Security settings (JWT, passwords)
- 🗄️ Database configuration (PostgreSQL with connection pooling)
- 💾 Redis caching
- 🔍 Elasticsearch full-text search
- 📧 Email/SMTP
- 💬 Messengers (Telegram, WhatsApp)
- 🤖 AI integration (OpenAI, Anthropic)
- 📊 Monitoring (Prometheus, Grafana)
- 📝 Logging & backups
- ...and 380+ lines of comprehensive config

### Required Environment Variables

| Variable | Description | Example | Generation |
|----------|-------------|---------|------------|
| `JWT_SECRET` | JWT signing secret (min 48 chars) | - | `openssl rand -base64 48` |
| `DB_PASSWORD` | Database password | - | `openssl rand -base64 32` |

### Optional Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8080 | Application port |
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
| `OPENAI_API_KEY` | - | OpenAI API key (for GPT-4) |
| `ANTHROPIC_API_KEY` | - | Anthropic API key (for Claude) |
| `TELEGRAM_BOT_TOKEN` | - | Telegram bot token |
| `WHATSAPP_ACCESS_TOKEN` | - | WhatsApp Business API token |

**Configuration files:**
- `.env.example` - Development configuration (quick start)
- `.env.production.example` - Production configuration (complete, 380+ lines)

---

## 📊 Monitoring

### Actuator Endpoints

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Info**: http://localhost:8080/actuator/info

### Grafana Dashboard

Access Grafana at **http://localhost:3000**

**Default credentials:** admin / admin

Pre-configured dashboards:
- Application metrics
- JVM statistics
- Database connection pool
- HTTP request metrics
- Custom business metrics

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok annotations
- Write comprehensive tests
- Update documentation

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- All contributors and supporters

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/qahhor/FREE-SERICE-DESK/issues)
- **Discussions**: [GitHub Discussions](https://github.com/qahhor/FREE-SERICE-DESK/discussions)
- **Documentation**: [Wiki](https://github.com/qahhor/FREE-SERICE-DESK/wiki)

---

## 📈 Project Status

| Metric | Status |
|--------|--------|
| Build | ![Build Status](https://img.shields.io/badge/build-passing-brightgreen) |
| Coverage | ![Coverage](https://img.shields.io/badge/coverage-85%25-green) |
| Version | ![Version](https://img.shields.io/badge/version-1.0.0--SNAPSHOT-blue) |
| Architecture | ![Architecture](https://img.shields.io/badge/architecture-monolithic-blue) |

---

<div align="center">

**Made with ❤️ by [Green White Solutions](https://greenwhite.uz)**

⭐ Star us on GitHub — it motivates us a lot!

[Website](https://greenwhite.uz) • [Documentation](./README.MONOLITH.md) • [Report Bug](https://github.com/qahhor/FREE-SERICE-DESK/issues) • [Request Feature](https://github.com/qahhor/FREE-SERICE-DESK/issues)

</div>
