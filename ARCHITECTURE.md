# ServiceDesk Platform - Architecture Documentation

## Table of Contents

- [Overview](#overview)
- [Architectural Evolution](#architectural-evolution)
- [Modular Monolithic Architecture](#modular-monolithic-architecture)
- [Module Descriptions](#module-descriptions)
- [Communication Patterns](#communication-patterns)
- [Data Architecture](#data-architecture)
- [Security Architecture](#security-architecture)
- [Integration Architecture](#integration-architecture)
- [Scalability & Performance](#scalability--performance)
- [Design Decisions](#design-decisions)

---

## Overview

ServiceDesk Platform is built using a **Modular Monolithic Architecture** - combining the operational simplicity of a monolith with the organizational benefits of microservices.

### Key Characteristics

- **Single Deployable Unit**: One JAR file contains all functionality
- **Modular Organization**: Clear module boundaries and responsibilities
- **Event-Driven**: Spring Events for inter-module communication
- **Domain-Driven Design**: Modules organized around business capabilities
- **Shared Database**: Single PostgreSQL database with proper schema design

---

## Architectural Evolution

### From Microservices to Modular Monolith

#### Previous Architecture (Microservices)

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│ API Gateway │────▶│ Ticket       │────▶│ PostgreSQL   │
│   (8080)    │     │ Service      │     │ (tickets DB) │
└─────────────┘     │   (8081)     │     └──────────────┘
       │            └──────────────┘
       │                   ▲
       │                   │ RabbitMQ
       ├────▶┌──────────────┐     │
       │     │ Channel      │◀────┘
       │     │ Service      │
       │     │   (8082)     │
       │     └──────────────┘
       │           ...
       │     (7 more services)
```

**Challenges:**
- Complex deployment and orchestration
- Network latency between services
- Distributed transactions difficult
- High infrastructure overhead
- Debugging across services complex

#### Current Architecture (Modular Monolith)

```
┌──────────────────────────────────────────────────┐
│        ServiceDesk Monolith (Port 8080)         │
├──────────────────────────────────────────────────┤
│                                                  │
│  ┌────────────┐  ┌────────────┐  ┌───────────┐ │
│  │   Ticket   │  │  Channel   │  │ Knowledge │ │
│  │   Module   │  │   Module   │  │   Module  │ │
│  └────────────┘  └────────────┘  └───────────┘ │
│                                                  │
│  ┌────────────┐  ┌────────────┐  ┌───────────┐ │
│  │     AI     │  │ Notif.     │  │ Analytics │ │
│  │   Module   │  │   Module   │  │   Module  │ │
│  └────────────┘  └────────────┘  └───────────┘ │
│                                                  │
│  ┌──────────────────────────────────────────┐  │
│  │         Marketplace Module               │  │
│  └──────────────────────────────────────────┘  │
│                                                  │
│  ┌──────────────────────────────────────────┐  │
│  │      Spring Events Bus (In-Memory)       │  │
│  └──────────────────────────────────────────┘  │
│                                                  │
│  ┌──────────────────────────────────────────┐  │
│  │        Common Infrastructure             │  │
│  │    (Security, Config, Utilities)         │  │
│  └──────────────────────────────────────────┘  │
│                                                  │
└──────────────────────────────────────────────────┘
            ↓              ↓           ↓
       PostgreSQL      Redis    Elasticsearch
```

**Benefits:**
- ✅ Simplified deployment
- ✅ Direct method calls (no network overhead)
- ✅ ACID transactions across modules
- ✅ Easier debugging and testing
- ✅ Lower operational complexity

### Migration Metrics

| Aspect | Microservices | Monolith | Improvement |
|--------|---------------|----------|-------------|
| **Startup Time** | ~90 seconds | ~30 seconds | **3x faster** |
| **Memory Usage** | ~4 GB | ~1.5 GB | **63% reduction** |
| **Docker Image Size** | ~2 GB (9 images) | ~250 MB (1 image) | **88% reduction** |
| **API Latency** | ~45 ms | ~12 ms | **73% faster** |
| **Deployment Time** | ~5 minutes | ~1 minute | **5x faster** |
| **Infrastructure Components** | 12+ | 5 | **58% reduction** |

---

## Modular Monolithic Architecture

### Core Principles

1. **Module Independence**: Each module has its own package with clear boundaries
2. **Interface-Based Communication**: Modules interact through well-defined interfaces
3. **Event-Driven**: Loose coupling via Spring Events
4. **Shared Nothing (Logical)**: Modules don't directly access each other's data
5. **Single Responsibility**: Each module focuses on a specific business capability

### Module Structure

Each module follows this structure:

```
module-name/
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # Data access
├── domain/          # Entity classes
├── dto/             # Data Transfer Objects
├── mapper/          # DTO ↔ Entity mapping
├── event/           # Module-specific events
└── config/          # Module configuration
```

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         API Layer (Controllers)         │  ← REST endpoints
├─────────────────────────────────────────┤
│     Application Layer (Services)        │  ← Business logic
├─────────────────────────────────────────┤
│       Domain Layer (Entities)           │  ← Domain models
├─────────────────────────────────────────┤
│  Infrastructure Layer (Repositories)    │  ← Data access
└─────────────────────────────────────────┘
```

---

## Module Descriptions

### 1. Ticket Module

**Responsibility**: Core ITSM functionality

**Components**:
- Ticket Management (CRUD, assignment, lifecycle)
- User Management (authentication, authorization)
- Team & Project Management
- SLA Management (policies, targets, breaches)
- Asset/CMDB Management
- Change Management
- Problem Management
- Automation Engine
- Escalation Rules

**Key Entities**:
- `Ticket`, `TicketComment`, `TicketAttachment`, `TicketHistory`
- `User`, `Team`, `Project`, `Category`
- `SlaPolicy`, `SlaTarget`, `SlaCondition`, `SlaBreachHistory`
- `Asset`, `AssetCategory`, `AssetRelationship`
- `ChangeRequest`, `ChangeApproval`, `ChangeHistory`
- `Problem`, `KnownError`, `ProblemRca`
- `AutomationRule`, `EscalationRule`

**API Endpoints**: `/api/v1/tickets/*`, `/api/v1/users/*`, `/api/v1/sla/*`, etc.

### 2. Channel Module

**Responsibility**: Omnichannel communication

**Components**:
- Email Integration (IMAP/SMTP polling)
- Telegram Bot
- WhatsApp Business API
- Live Chat (WebSocket-based)
- Web Widget (embeddable)

**Key Entities**:
- `Channel`, `EmailConfiguration`, `EmailMessage`
- `TelegramConfiguration`, `TelegramMessage`
- `WhatsAppConfiguration`, `WhatsAppMessage`, `WhatsAppContact`
- `LiveChatSession`, `LiveChatMessage`
- `WidgetConversation`, `WidgetMessage`

**API Endpoints**: `/api/v1/channels/*`, `/api/v1/widget/*`, `/api/v1/livechat/*`

### 3. Notification Module

**Responsibility**: Centralized notification system

**Components**:
- In-app Notifications
- Email Notifications (with templates)
- Push Notifications (FCM)
- SMS Notifications (Twilio)
- Notification Preferences

**Key Entities**:
- `Notification`
- `NotificationPreference`

**API Endpoints**: `/api/v1/notifications/*`

### 4. Knowledge Module

**Responsibility**: Knowledge base management

**Components**:
- Article Management
- Category Organization
- Full-text Search (Elasticsearch)
- Article Versioning
- Feedback & Analytics

**Key Entities**:
- `Article`
- `ArticleCategory`
- `ArticleDocument` (Elasticsearch)

**API Endpoints**: `/api/v1/knowledge/*`

### 5. AI Module

**Responsibility**: AI/ML capabilities

**Components**:
- AI Chat (OpenAI, Claude)
- Ticket Analysis & Categorization
- Response Generation
- Text Summarization
- Translation
- RAG (Retrieval Augmented Generation)
- Embeddings Generation

**Providers**:
- `OpenAiProvider` (GPT-4, text-embedding-3-small)
- `ClaudeProvider` (Claude 3 Sonnet/Opus)

**API Endpoints**: `/api/v1/ai/*`

### 6. Analytics Module

**Responsibility**: Metrics and reporting

**Components**:
- Dashboard Metrics
- Agent Performance
- Team Performance
- SLA Compliance Reports
- Report Generation (PDF, XLSX, CSV)
- Real-time Metrics

**API Endpoints**: `/api/v1/analytics/*`

### 7. Marketplace Module

**Responsibility**: Plugin/module system

**Components**:
- Module Discovery
- Module Installation/Uninstallation
- Module Lifecycle Management
- Dynamic Endpoint Registration
- Module API (for plugin developers)

**Key Entities**:
- `MarketplaceModule`
- `ModuleVersion`
- `ModuleInstallation`
- `ModuleReview`

**API Endpoints**: `/api/v1/marketplace/*`

---

## Communication Patterns

### 1. Synchronous Communication

**Direct Method Calls**

Modules communicate via direct service method invocations:

```java
@Service
public class TicketService {
    @Autowired
    private NotificationService notificationService;

    public void assignTicket(UUID ticketId, UUID assigneeId) {
        // Business logic
        ticket.setAssignee(assigneeId);
        ticketRepository.save(ticket);

        // Direct call to notification service
        notificationService.sendNotification(
            assigneeId,
            "Ticket Assigned",
            "You have been assigned a new ticket"
        );
    }
}
```

**Benefits**:
- No network overhead
- Strong typing and compile-time checking
- IDE support (autocomplete, refactoring)
- Easy debugging

### 2. Asynchronous Communication

**Spring Events**

For loose coupling and asynchronous processing:

```java
// Publishing an event
@Service
public class TicketService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void createTicket(CreateTicketRequest request) {
        Ticket ticket = // ... create ticket
        ticketRepository.save(ticket);

        // Publish event
        eventPublisher.publishEvent(
            TicketCreatedEvent.builder()
                .ticketId(ticket.getId())
                .subject(ticket.getSubject())
                .priority(ticket.getPriority())
                .build()
        );
    }
}

// Listening to event
@Service
public class NotificationService {
    @EventListener
    @Async  // Runs in separate thread
    public void handleTicketCreated(TicketCreatedEvent event) {
        // Send notifications
    }
}
```

**Event Types**:
1. `NotificationEvent` - Send notifications
2. `TicketCreatedEvent` - Ticket created from channel
3. `EmailEvent` - Send emails
4. `WebhookEvent` - Trigger webhooks
5. `EscalationEvent` - Ticket escalation
6. `AutomationEvent` - Trigger automation
7. `ModuleEvent` - Module lifecycle
8. `WidgetNotificationEvent` - Real-time widget updates

**Benefits**:
- Loose coupling between modules
- Easy to add new listeners
- Supports @Async for non-blocking execution
- No external message broker needed

### 3. REST API

**External Communication**

For frontend and external integrations:

```
Frontend/External → HTTP → Controllers → Services → Repositories → Database
```

All API endpoints are under `/api/v1/` prefix.

---

## Data Architecture

### Database Design

**Single Database Approach**

```
servicedesk (PostgreSQL Database)
├── Ticket Module Tables
│   ├── tickets
│   ├── ticket_comments
│   ├── ticket_attachments
│   ├── users
│   ├── teams
│   ├── projects
│   ├── sla_policies
│   ├── assets
│   ├── change_requests
│   └── problems
│
├── Channel Module Tables
│   ├── channels
│   ├── email_configurations
│   ├── email_messages
│   ├── telegram_configurations
│   ├── whatsapp_messages
│   ├── livechat_sessions
│   └── widget_conversations
│
├── Notification Module Tables
│   ├── notifications
│   └── notification_preferences
│
├── Knowledge Module Tables
│   ├── articles
│   └── article_categories
│
└── Marketplace Module Tables
    ├── marketplace_modules
    ├── module_versions
    ├── module_installations
    └── module_reviews
```

### Flyway Migrations

Migrations are numbered to avoid conflicts:

- **V1-V9**: Ticket Module
- **V10-V19**: Channel Module
- **V20-V29**: Notification Module
- **V30-V39**: Knowledge Module
- **V40-V49**: Marketplace Module

### Transaction Management

**ACID Transactions**

All operations within a `@Transactional` method are atomic:

```java
@Service
@Transactional
public class TicketService {
    public void transferTicket(UUID ticketId, UUID newProjectId) {
        // All-or-nothing operation
        ticket.setProject(newProjectId);
        ticketRepository.save(ticket);

        ticketHistoryRepository.save(
            new TicketHistory(ticketId, "Transferred", ...)
        );

        // If any operation fails, everything rolls back
    }
}
```

### Caching Strategy

**Redis Caching**

```java
@Service
public class UserService {
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

**Cache Configuration**:
- TTL: 10 minutes (default)
- Eviction: LRU
- Serialization: JSON

### Search Architecture

**Elasticsearch Integration**

For full-text search and RAG:

```
Knowledge Module
    ↓
[PostgreSQL] ← Master data
    ↓ (indexed)
[Elasticsearch] ← Search & RAG
```

---

## Security Architecture

### Authentication & Authorization

**JWT-Based Authentication**

```
User → Login → AuthController → AuthService
                                    ↓
                            Generate JWT Token
                                    ↓
                        Return Token to User
                                    ↓
User → API Request + Token → JwtAuthenticationFilter
                                    ↓
                            Validate Token
                                    ↓
                        Set SecurityContext
                                    ↓
                            Controller
```

**JWT Token Structure**:
```json
{
  "sub": "user-id",
  "email": "user@example.com",
  "roles": ["ROLE_ADMIN"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

### Authorization

**Role-Based Access Control (RBAC)**

```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        // Only admins can access
    }
}
```

**Roles**:
- `ADMIN` - Full system access
- `AGENT` - Ticket management
- `CUSTOMER` - Self-service portal
- `MANAGER` - Team management

### Security Headers

Configured in `SecurityConfig`:
- CORS
- CSRF Protection (for web)
- XSS Protection
- Content Security Policy

---

## Integration Architecture

### External Integrations

#### 1. Email (IMAP/SMTP)

```
Email Server ← Polling (scheduled) ← EmailPollingScheduler
    ↓
Parse Email
    ↓
Publish TicketCreatedEvent
    ↓
TicketService creates ticket
```

#### 2. Telegram Bot

```
Telegram API → Webhook → TelegramController
    ↓
Parse Message
    ↓
Publish TicketCreatedEvent
```

#### 3. WhatsApp Business

```
WhatsApp API → Webhook → WhatsAppController
    ↓
Parse Message
    ↓
Create/Update Ticket
```

#### 4. AI Services

```
User Request → AiController → AiService
    ↓
Select Provider (OpenAI/Claude)
    ↓
Call External API
    ↓
Return Response
```

---

## Scalability & Performance

### Vertical Scaling

**Resource Allocation**:
- CPU: 2-4 cores (recommended)
- Memory: 2-4 GB heap
- Database connections: 20 (max pool size)

**JVM Tuning**:
```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar servicedesk-monolith.jar
```

### Horizontal Scaling

**Load Balancing**:

```
Load Balancer (Nginx/HAProxy)
    ↓        ↓        ↓
Instance 1  Instance 2  Instance 3
    ↓        ↓        ↓
Shared PostgreSQL
```

**Session Management**:
- Stateless (JWT tokens)
- No session affinity needed

### Caching Strategy

**Multi-Level Caching**:

```
Request → L1: Application Cache (Caffeine)
              ↓ (miss)
          L2: Redis Cache
              ↓ (miss)
          L3: Database
```

### Database Optimization

**Connection Pooling**:
- HikariCP (default in Spring Boot)
- Max pool size: 20
- Connection timeout: 30s

**Query Optimization**:
- Indexed columns: email, ticket number, status
- Pagination for large result sets
- Query hints for complex queries

---

## Design Decisions

### Why Monolith Over Microservices?

**Decision**: Transform from microservices to modular monolith

**Rationale**:
1. **Operational Simplicity**: Single deployment artifact
2. **Performance**: Eliminated network latency
3. **Development Speed**: Faster iteration
4. **Cost**: Lower infrastructure costs
5. **Team Size**: Better suited for small-medium teams

**Trade-offs**:
- Less fine-grained scaling
- Tighter coupling (mitigated by modular design)
- Single point of failure (mitigated by load balancing)

### Why Spring Events Over RabbitMQ?

**Decision**: Use Spring Events instead of RabbitMQ

**Rationale**:
1. **In-Process**: No network overhead
2. **Simpler**: No external broker to manage
3. **Sufficient**: Async processing still possible with @Async
4. **ACID**: Better transaction support

**Trade-offs**:
- No message persistence (not needed for our use case)
- No distributed messaging (not needed in monolith)

### Why Single Database?

**Decision**: Use single PostgreSQL database

**Rationale**:
1. **ACID Transactions**: Full transactional support
2. **Simpler**: One database to manage
3. **Performance**: No cross-database joins
4. **Backups**: Single backup process

**Trade-offs**:
- Shared schema (mitigated by table naming)
- Single point of failure (mitigated by replication)

### Why Keep Elasticsearch?

**Decision**: Retain Elasticsearch despite being "stateful"

**Rationale**:
1. **Full-Text Search**: Superior to PostgreSQL for text search
2. **RAG**: Essential for AI features
3. **Performance**: Optimized for search workloads
4. **Scalability**: Easy to scale horizontally

---

## Future Considerations

### Potential Evolution Paths

1. **Microservices (if needed)**:
   - Module boundaries already defined
   - Can extract modules into services
   - Events already loosely coupled

2. **CQRS**:
   - Separate read/write models
   - Event sourcing for audit trail

3. **Multi-Tenancy**:
   - Add tenant_id to all tables
   - Row-level security

4. **Serverless Functions**:
   - Extract heavy computations (AI, reports)
   - Deploy as AWS Lambda/Azure Functions

---

## Conclusion

The Modular Monolithic Architecture provides an excellent balance between:
- **Simplicity**: Easy to develop, deploy, and maintain
- **Performance**: Low latency, high throughput
- **Flexibility**: Can evolve to microservices if needed
- **Cost**: Lower infrastructure and operational costs

This architecture is particularly well-suited for:
- Small to medium teams
- Applications with CRUD-heavy workloads
- Systems requiring strong consistency
- Projects prioritizing developer productivity

---

**Last Updated**: 2024
**Architecture Version**: 2.0 (Modular Monolith)
