# Contributing to ServiceDesk Platform

Thank you for your interest in contributing to ServiceDesk Platform! This document provides guidelines and information for contributors.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct. Please be respectful and constructive in all interactions.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/your-org/servicedesk-platform/issues)
2. If not, create a new issue with:
   - Clear, descriptive title
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Environment details (OS, Java version, etc.)

### Suggesting Features

1. Check existing [Issues](https://github.com/your-org/servicedesk-platform/issues) and [Discussions](https://github.com/your-org/servicedesk-platform/discussions)
2. Create a new discussion in the "Ideas" category
3. Describe:
   - The problem you're trying to solve
   - Your proposed solution
   - Alternative approaches considered

### Pull Requests

1. Fork the repository
2. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Make your changes following our coding standards
4. Write/update tests as needed
5. Update documentation if required
6. Commit with clear, descriptive messages:
   ```bash
   git commit -m "feat: add ticket merging functionality"
   ```
7. Push to your fork and create a Pull Request

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+ (for frontend)
- IDE with Lombok support (IntelliJ IDEA or Eclipse)

### Setup Steps

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/servicedesk-platform.git
cd servicedesk-platform

# Start infrastructure (PostgreSQL, Redis, Elasticsearch, MinIO)
docker-compose -f docker-compose.monolith.yml up -d postgres redis elasticsearch minio

# Build the monolithic application
cd backend
mvn clean install -pl monolith-app -am

# Run the monolith locally
cd monolith-app
mvn spring-boot:run

# Or run the JAR directly
java -jar target/servicedesk-monolith.jar

# Access the application
# API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Actuator Health: http://localhost:8080/actuator/health
```

### Running with Docker (Full Stack)

```bash
# Build and run everything
docker-compose -f docker-compose.monolith.yml up -d

# View logs
docker-compose -f docker-compose.monolith.yml logs -f servicedesk-monolith

# Stop all services
docker-compose -f docker-compose.monolith.yml down
```

## Coding Standards

### Java
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write Javadoc for public APIs
- Keep methods small and focused
- Use Lombok annotations appropriately

### Commit Messages
Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Maintenance tasks

### Testing
- Write unit tests for all new code
- Maintain >70% code coverage
- Use meaningful test names
- Test edge cases and error conditions

```bash
# Run all tests
cd backend
mvn test -pl monolith-app

# Run with coverage report
mvn test jacoco:report -pl monolith-app

# Run integration tests (uses Testcontainers)
mvn verify -pl monolith-app

# View coverage report
open monolith-app/target/site/jacoco/index.html
```

### Working with the Modular Monolith

#### Adding a New Feature to an Existing Module

1. **Identify the module** - Determine which module owns the feature
2. **Create/update entities** - Add domain models in `{module}/entity/`
3. **Add repository** - Create JPA repository in `{module}/repository/`
4. **Implement service** - Add business logic in `{module}/service/`
5. **Create controller** - Add REST endpoints in `{module}/controller/`
6. **Write tests** - Add unit and integration tests
7. **Update documentation** - Add Swagger annotations

#### Creating a New Module

If you need to add a completely new module:

1. Create package structure: `com.servicedesk.monolith.{module-name}/`
2. Follow the standard module layout (entity, repository, service, controller)
3. Create module configuration if needed
4. Register module in main application
5. Add database migrations in `resources/db/migration/`
6. Update documentation in ARCHITECTURE.md

#### Inter-Module Communication

**Synchronous (Direct calls):**
```java
@Service
@RequiredArgsConstructor
public class TicketService {
    private final UserService userService;  // Direct dependency

    public void assignTicket(UUID ticketId, UUID userId) {
        User user = userService.findById(userId);  // Direct call
        // ... business logic
    }
}
```

**Asynchronous (Events):**
```java
// Publishing an event
@Service
@RequiredArgsConstructor
public class TicketService {
    private final ApplicationEventPublisher eventPublisher;

    public void createTicket(Ticket ticket) {
        // ... save ticket
        eventPublisher.publishEvent(NotificationEvent.builder()
            .userId(ticket.getAssigneeId())
            .type("EMAIL")
            .title("New Ticket Assigned")
            .build());
    }
}

// Listening to an event
@Component
public class NotificationEventListener {
    @EventListener
    @Async
    public void handleNotification(NotificationEvent event) {
        // Process notification
    }
}
```

#### Database Migrations

When adding database changes:

1. Create new migration file: `V{next-number}__{description}.sql`
2. Follow naming convention: `V42__add_custom_fields_to_tickets.sql`
3. Test migration on clean database
4. Test rollback if possible
5. Document any manual steps needed

Example:
```sql
-- V42__add_custom_fields_to_tickets.sql
ALTER TABLE tickets ADD COLUMN custom_fields JSONB;
CREATE INDEX idx_tickets_custom_fields ON tickets USING gin(custom_fields);
```

## Project Structure

The project follows a **modular monolithic architecture** with clear module boundaries:

```
backend/
├── common-lib/                    # Shared utilities, DTOs, security
│   ├── entity/                   # Base entities
│   ├── dto/                      # Common DTOs
│   ├── exception/                # Exception classes
│   ├── security/                 # JWT, authentication
│   └── config/                   # Common configurations
│
└── monolith-app/                  # 🎯 Unified monolithic application
    ├── src/main/java/com/servicedesk/monolith/
    │   ├── ServiceDeskMonolithApplication.java  # Main entry point
    │   │
    │   ├── ticket/               # Ticket Management Module
    │   │   ├── entity/          # Ticket, User, Team, SLA entities
    │   │   ├── dto/             # Request/Response DTOs
    │   │   ├── repository/      # JPA repositories
    │   │   ├── service/         # Business logic
    │   │   ├── controller/      # REST endpoints
    │   │   └── config/          # Module-specific configs
    │   │
    │   ├── channel/              # Omnichannel Module
    │   │   ├── entity/          # Channel, Message entities
    │   │   ├── service/         # Email, Telegram, WhatsApp services
    │   │   └── controller/      # Channel endpoints
    │   │
    │   ├── notification/         # Notification Module
    │   │   ├── entity/          # Notification entities
    │   │   ├── service/         # Notification delivery
    │   │   └── event/           # Event listeners
    │   │
    │   ├── knowledge/            # Knowledge Base Module
    │   │   ├── entity/          # Article, Category entities
    │   │   ├── service/         # Full-text search
    │   │   └── controller/      # Knowledge API
    │   │
    │   ├── ai/                   # AI Module
    │   │   ├── service/         # OpenAI, Claude integration
    │   │   └── controller/      # AI endpoints
    │   │
    │   ├── analytics/            # Analytics Module
    │   │   ├── service/         # Dashboard, reports
    │   │   └── controller/      # Analytics API
    │   │
    │   ├── marketplace/          # Marketplace Module
    │   │   ├── entity/          # Module, Plugin entities
    │   │   └── service/         # Plugin system
    │   │
    │   └── common/               # Common configurations
    │       ├── config/          # Database, security, cache
    │       └── event/           # Spring events (replaces RabbitMQ)
    │
    └── src/main/resources/
        ├── application.yml
        ├── application-docker.yml
        └── db/migration/        # Flyway migrations (V1-V41)
```

### Module Boundaries

Each module has:
- **Clear responsibility** - Single purpose (SRP)
- **Independent domain** - Own entities and business logic
- **Defined interfaces** - Service layer for module interactions
- **Event-driven communication** - Spring Events for async operations

## API Guidelines

- Use RESTful conventions
- Version APIs (`/api/v1/...`)
- Return consistent response format
- Use proper HTTP status codes
- Document with OpenAPI annotations

## Review Process

1. All PRs require at least one approval
2. CI checks must pass
3. Code coverage should not decrease
4. Documentation must be updated

## Getting Help

- Join our [Discord](https://discord.gg/servicedesk)
- Ask in [GitHub Discussions](https://github.com/your-org/servicedesk-platform/discussions)
- Check the [Wiki](https://github.com/your-org/servicedesk-platform/wiki)

## Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project documentation

Thank you for contributing!
