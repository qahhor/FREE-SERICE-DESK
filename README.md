# ServiceDesk Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17+-red.svg)](https://angular.io/)

Open-source Service Desk Platform for Customer Support - Built with Java Spring Boot and Angular.

## Features

### Core ITSM Features
- **Ticket Management**: Create, assign, track, and resolve support tickets
- **SLA Management**: Configurable SLA policies with escalation rules
- **Asset/CMDB Management**: Track IT assets, relationships, and maintenance
- **Change Management**: Change requests with approval workflows
- **Problem Management**: Root cause analysis with known error database

### Omnichannel Support
- **Email Integration**: Auto-create tickets from emails
- **Telegram Bot**: Customer support via Telegram
- **WhatsApp Business**: WhatsApp integration
- **Live Chat**: Embeddable web widget
- **Customer Portal**: Self-service portal

### AI-Powered
- **AI Agent**: Integration with OpenAI/Claude for intelligent responses
- **RAG**: Retrieval Augmented Generation for knowledge base
- **Smart Categorization**: Automatic ticket classification
- **Sentiment Analysis**: Customer sentiment detection

### Analytics & Reporting
- **Dashboards**: Real-time metrics and KPIs
- **Reports**: CSAT, NPS, FRT, ART, SLA compliance
- **Export**: CSV, Excel, PDF formats

### Multi-language Support
- English (en), Russian (ru), Uzbek (uz), Kazakh (kk), Arabic (ar) - RTL support

## Tech Stack

### Backend (Microservices)
| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Single entry point |
| Ticket Service | 8081 | Core ticket management |
| Channel Service | 8082 | Omnichannel integration |
| Knowledge Service | 8083 | Knowledge base |
| Notification Service | 8084 | Email/Push notifications |
| AI Service | 8085 | LLM/RAG integration |
| Marketplace Service | 8086 | Module marketplace |
| Analytics Service | 8087 | Metrics & Reporting |

### Infrastructure
- **PostgreSQL 16** - Primary database
- **Redis 7** - Caching and sessions
- **Elasticsearch 8** - Full-text search
- **RabbitMQ** - Message queue
- **MinIO** - Object storage (S3 compatible)

### Frontend
- **Angular 17+** - Agent and Admin apps
- **Customer Portal** - Self-service
- **Web Widget** - Embeddable chat

### DevOps
- **Docker** & **Docker Compose**
- **Prometheus** & **Grafana** - Monitoring

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for development)
- Maven 3.8+ (for development)

### Production Deployment

```bash
# 1. Clone the repository
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform

# 2. Configure environment
cp .env.example .env
# Edit .env - set JWT_SECRET (required!)

# 3. Start all services
docker-compose up -d

# 4. Access the application
# API: http://localhost:8080
# Grafana: http://localhost:3000
```

### Development

```bash
# Start infrastructure + all ports exposed
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Additional dev ports:
# - PostgreSQL: 5432
# - Redis: 6379
# - RabbitMQ UI: 15672
# - Elasticsearch: 9200
# - MinIO Console: 9001
# - MailHog: 8025
```

### Default Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@servicedesk.local | admin123 |
| Agent | agent1@servicedesk.local | admin123 |

## Project Structure

```
servicedesk-platform/
├── backend/
│   ├── common-lib/           # Shared utilities and DTOs
│   ├── api-gateway/          # Spring Cloud Gateway (port 8080)
│   ├── ticket-service/       # Core ticket management (port 8081)
│   ├── channel-service/      # Email, Telegram, WhatsApp, Live Chat (port 8082)
│   ├── knowledge-service/    # Knowledge base & Search (port 8083)
│   ├── notification-service/ # Email/Push notifications (port 8084)
│   ├── ai-service/           # LLM/RAG integration (port 8085)
│   ├── marketplace-service/  # Module marketplace (port 8086)
│   ├── analytics-service/    # Metrics & Reporting (port 8087)
│   └── modules/              # Pluggable modules
│
├── frontend/
│   ├── agent-app/            # Agent interface
│   ├── customer-portal/      # Self-service portal
│   └── widget/               # Embeddable chat widget
│
├── infrastructure/
│   └── prometheus/           # Monitoring configs
│
├── scripts/
│   └── init-databases.sql    # DB initialization
│
├── docker-compose.yml        # Production (2 ports: 8080, 3000)
└── docker-compose.dev.yml    # Development (all ports)
```

## API Documentation

### Authentication
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@servicedesk.local",
  "password": "admin123"
}
```

### Create Ticket
```http
POST /api/v1/tickets
Authorization: Bearer <token>
Content-Type: application/json

{
  "subject": "Cannot login to application",
  "description": "Getting error 401 when trying to login",
  "projectId": "uuid",
  "priority": "HIGH",
  "type": "INCIDENT"
}
```

### API Endpoints
| Endpoint | Description |
|----------|-------------|
| `/api/v1/auth/*` | Authentication |
| `/api/v1/tickets/*` | Ticket management |
| `/api/v1/users/*` | User management |
| `/api/v1/projects/*` | Project management |
| `/api/v1/sla/*` | SLA policies |
| `/api/v1/assets/*` | Asset management |
| `/api/v1/changes/*` | Change management |
| `/api/v1/problems/*` | Problem management |
| `/api/v1/knowledge/*` | Knowledge base |
| `/api/v1/notifications/*` | Notifications |
| `/api/v1/analytics/*` | Analytics & Reports |

**Swagger UI**: http://localhost:8080/swagger-ui.html

## Configuration

### Required Environment Variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | JWT signing secret (min 32 chars) |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_PASSWORD` | Database password | servicedesk |
| `RABBITMQ_PASSWORD` | RabbitMQ password | servicedesk |
| `MAIL_HOST` | SMTP server | smtp.gmail.com |
| `MAIL_USERNAME` | SMTP username | - |
| `MAIL_PASSWORD` | SMTP password | - |
| `OPENAI_API_KEY` | OpenAI API key | - |
| `ANTHROPIC_API_KEY` | Anthropic API key | - |

See `.env.example` for full list.

## Development

### Running Tests
```bash
cd backend

# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Building JAR
```bash
cd backend
mvn clean package -DskipTests

# JAR location: target/*.jar
```

### Database Migrations
Migrations are managed with Flyway:
```bash
# Migrations are in:
# backend/*/src/main/resources/db/migration/

# Run migrations automatically on startup
# or manually:
mvn flyway:migrate
```

## Monitoring

- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: Internal (exposed in dev mode on 9090)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: [GitHub Issues](https://github.com/your-org/servicedesk-platform/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/servicedesk-platform/discussions)

---

Made with ❤️ by [Green White Solutions](https://greenwhite.uz)
