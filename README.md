# ServiceDesk Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17+-red.svg)](https://angular.io/)

Open-source Service Desk Platform for Customer Support - Built with Java Spring Boot and Angular.

## Features

### Core Features
- **Ticket Management**: Create, assign, track, and resolve support tickets
- **Omnichannel Support**: Email, Telegram, WhatsApp, Web Widget, Phone
- **Knowledge Base**: Markdown articles with full-text search (Elasticsearch)
- **SLA Management**: Configurable SLA policies with escalation
- **Automation**: Rules engine for auto-assignment and workflows

### AI-Powered
- **AI Agent**: Integration with OpenAI/Claude for intelligent responses
- **RAG**: Retrieval Augmented Generation for knowledge base
- **Smart Categorization**: Automatic ticket classification
- **Sentiment Analysis**: Customer sentiment detection

### Analytics
- **Dashboards**: Real-time metrics and KPIs
- **Reports**: CSAT, NPS, FRT, ART, and custom reports
- **Export**: CSV, Excel, API for BI tools

### Multi-language Support
- English (en)
- Russian (ru)
- Uzbek (uz)
- Kazakh (kk)
- Arabic (ar) - RTL support

## Tech Stack

### Backend
- **Java 17+** with **Spring Boot 3.2**
- **PostgreSQL** - Primary database
- **Redis** - Caching and sessions
- **Elasticsearch** - Full-text search
- **RabbitMQ** - Message queue

### Frontend
- **Angular 17+**
- **Angular Material / PrimeNG**
- **NgRx** - State management
- **WebSocket** - Real-time updates

### DevOps
- **Docker** & **Docker Compose**
- **Kubernetes** with Helm charts
- **GitHub Actions** - CI/CD
- **Prometheus** & **Grafana** - Monitoring

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### 1. Clone the Repository
```bash
git clone https://github.com/your-org/servicedesk-platform.git
cd servicedesk-platform
```

### 2. Start Infrastructure Services
```bash
# Start PostgreSQL, Redis, and other services
docker-compose -f docker-compose.dev.yml up -d
```

### 3. Build and Run Backend
```bash
cd backend
mvn clean install -DskipTests

# Run ticket-service
cd ticket-service
mvn spring-boot:run
```

### 4. Access the Application
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **MailHog** (dev email): http://localhost:8025

### Default Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@servicedesk.local | admin123 |
| Agent | agent1@servicedesk.local | admin123 |
| Customer | customer@example.com | admin123 |

## Project Structure

```
servicedesk-platform/
├── backend/
│   ├── common-lib/           # Shared utilities and DTOs
│   ├── ticket-service/       # Core ticket management (Main API)
│   ├── channel-service/      # Omnichannel adapter
│   ├── telephony-service/    # VoIP/SIP integration
│   ├── ai-service/           # LLM/RAG integration
│   ├── analytics-service/    # Metrics & Reporting
│   ├── knowledge-service/    # KB & Search
│   ├── notification-service/ # Email/Push notifications
│   └── api-gateway/          # Spring Cloud Gateway
│
├── frontend/
│   ├── agent-app/            # Agent interface
│   ├── admin-app/            # Admin panel
│   ├── customer-portal/      # Self-service portal
│   └── web-widget/           # Embeddable chat widget
│
├── infrastructure/
│   ├── kubernetes/           # K8s manifests & Helm charts
│   ├── terraform/            # Infrastructure as Code
│   └── prometheus/           # Monitoring configs
│
├── docs/                     # Documentation
├── scripts/                  # Utility scripts
├── docker-compose.yml        # Full stack deployment
└── docker-compose.dev.yml    # Development services only
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
  "projectId": "f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "priority": "HIGH",
  "type": "INCIDENT"
}
```

### List Tickets
```http
GET /api/v1/tickets?status=OPEN&priority=HIGH&page=0&size=20
Authorization: Bearer <token>
```

For full API documentation, see [API.md](docs/API.md) or access Swagger UI at `/swagger-ui.html`.

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | servicedesk |
| `DB_USERNAME` | Database user | servicedesk |
| `DB_PASSWORD` | Database password | servicedesk |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `JWT_SECRET` | JWT signing secret | (auto-generated) |
| `CORS_ORIGINS` | Allowed CORS origins | http://localhost:4200 |

### Application Properties
See [application.yml](backend/ticket-service/src/main/resources/application.yml) for all configuration options.

## Development

### Running Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify -P integration-tests

# With coverage
mvn test jacoco:report
```

### Code Style
We follow Google Java Style Guide. Run the formatter:
```bash
mvn spotless:apply
```

### Database Migrations
Migrations are managed with Flyway:
```bash
# Create a new migration
touch backend/ticket-service/src/main/resources/db/migration/V3__your_migration.sql

# Run migrations
mvn flyway:migrate
```

## Deployment

### Docker
```bash
# Build and run all services
docker-compose up -d --build

# View logs
docker-compose logs -f ticket-service
```

### Kubernetes
```bash
# Using Helm
helm install servicedesk ./infrastructure/kubernetes/helm-charts/servicedesk \
  --namespace servicedesk \
  --create-namespace \
  --set postgres.password=your-secure-password
```

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Roadmap

### Phase 1 (MVP) - Q1 2025
- [x] Core ticket management
- [x] User authentication (JWT)
- [x] Basic REST API
- [ ] Email channel integration
- [ ] Web widget
- [ ] Basic dashboard

### Phase 2 - Q2 2025
- [ ] Telegram Bot integration
- [ ] WhatsApp Business API
- [ ] Knowledge base with Elasticsearch
- [ ] AI-powered responses
- [ ] SLA management

### Phase 3 - Q3 2025
- [ ] VoIP/Telephony integration
- [ ] Advanced analytics
- [ ] Mobile apps (Flutter)
- [ ] Multi-tenant architecture

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Documentation**: [docs.servicedesk.io](https://docs.servicedesk.io)
- **Issues**: [GitHub Issues](https://github.com/your-org/servicedesk-platform/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/servicedesk-platform/discussions)

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Angular](https://angular.io/)
- [PostgreSQL](https://www.postgresql.org/)
- [Elasticsearch](https://www.elastic.co/)

---

Made with love by [Green White Solutions](https://greenwhite.uz)
