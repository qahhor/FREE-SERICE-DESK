# ServiceDesk Frontend - Angular Implementation Guide

Comprehensive guide for implementing the complete Angular frontend for the ServiceDesk monolithic application.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [API Integration](#api-integration)
- [Implementation Roadmap](#implementation-roadmap)
- [Development Guide](#development-guide)

## 🎯 Project Overview

**Frontend Stack:**
- Angular 17+ (Latest)
- Angular Material UI
- RxJS for reactive programming
- Chart.js for analytics
- Socket.io for real-time features
- Quill for rich text editing

**Backend Integration:**
- REST API: `http://localhost:8080/api/v1`
- WebSocket: `ws://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🚀 Quick Start

### Prerequisites

```bash
# Install Node.js 18+ and npm
node --version  # v18.0.0+
npm --version   # 9.0.0+
```

### Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start

# Access at http://localhost:4200
```

### Build for Production

```bash
# Build with production configuration
npm run build:prod

# Output: dist/servicedesk-frontend/
```

## 🏗️ Architecture

### Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                    # Core module (singleton services)
│   │   │   ├── auth/               # Authentication
│   │   │   │   ├── guards/
│   │   │   │   │   └── auth.guard.ts
│   │   │   │   ├── interceptors/
│   │   │   │   │   ├── auth.interceptor.ts
│   │   │   │   │   └── error.interceptor.ts
│   │   │   │   └── services/
│   │   │   │       └── auth.service.ts
│   │   │   ├── services/          # Core services
│   │   │   │   ├── api.service.ts
│   │   │   │   ├── websocket.service.ts
│   │   │   │   └── notification.service.ts
│   │   │   └── core.module.ts
│   │   │
│   │   ├── shared/                 # Shared module (reusable components)
│   │   │   ├── components/
│   │   │   │   ├── header/
│   │   │   │   ├── sidebar/
│   │   │   │   ├── footer/
│   │   │   │   ├── breadcrumb/
│   │   │   │   ├── data-table/
│   │   │   │   ├── loading-spinner/
│   │   │   │   └── confirm-dialog/
│   │   │   ├── pipes/
│   │   │   │   ├── date-format.pipe.ts
│   │   │   │   ├── file-size.pipe.ts
│   │   │   │   └── highlight.pipe.ts
│   │   │   ├── directives/
│   │   │   │   ├── autofocus.directive.ts
│   │   │   │   └── permission.directive.ts
│   │   │   ├── models/
│   │   │   │   └── base.model.ts
│   │   │   └── shared.module.ts
│   │   │
│   │   ├── features/              # Feature modules
│   │   │   │
│   │   │   ├── auth/              # Authentication module
│   │   │   │   ├── login/
│   │   │   │   ├── register/
│   │   │   │   ├── forgot-password/
│   │   │   │   └── auth-routing.module.ts
│   │   │   │
│   │   │   ├── dashboard/         # Main dashboard
│   │   │   │   ├── dashboard.component.ts
│   │   │   │   ├── widgets/
│   │   │   │   └── dashboard.module.ts
│   │   │   │
│   │   │   ├── tickets/           # Ticket Management Module
│   │   │   │   ├── models/
│   │   │   │   │   └── ticket.model.ts
│   │   │   │   ├── services/
│   │   │   │   │   └── ticket.service.ts
│   │   │   │   ├── components/
│   │   │   │   │   ├── ticket-list/
│   │   │   │   │   ├── ticket-detail/
│   │   │   │   │   ├── ticket-create/
│   │   │   │   │   ├── ticket-edit/
│   │   │   │   │   └── ticket-comments/
│   │   │   │   ├── tickets-routing.module.ts
│   │   │   │   └── tickets.module.ts
│   │   │   │
│   │   │   ├── channels/          # Channel Management Module
│   │   │   │   ├── models/
│   │   │   │   ├── services/
│   │   │   │   ├── components/
│   │   │   │   │   ├── email-channel/
│   │   │   │   │   ├── telegram-channel/
│   │   │   │   │   ├── whatsapp-channel/
│   │   │   │   │   ├── livechat/
│   │   │   │   │   └── widget-config/
│   │   │   │   └── channels.module.ts
│   │   │   │
│   │   │   ├── knowledge/         # Knowledge Base Module
│   │   │   │   ├── models/
│   │   │   │   ├── services/
│   │   │   │   ├── components/
│   │   │   │   │   ├── article-list/
│   │   │   │   │   ├── article-view/
│   │   │   │   │   ├── article-editor/
│   │   │   │   │   ├── category-tree/
│   │   │   │   │   └── search/
│   │   │   │   └── knowledge.module.ts
│   │   │   │
│   │   │   ├── ai/                # AI Module
│   │   │   │   ├── models/
│   │   │   │   ├── services/
│   │   │   │   ├── components/
│   │   │   │   │   ├── ai-chat/
│   │   │   │   │   ├── suggestions/
│   │   │   │   │   ├── sentiment-analysis/
│   │   │   │   │   └── auto-response/
│   │   │   │   └── ai.module.ts
│   │   │   │
│   │   │   ├── analytics/         # Analytics Module
│   │   │   │   ├── models/
│   │   │   │   ├── services/
│   │   │   │   ├── components/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   ├── reports/
│   │   │   │   │   ├── charts/
│   │   │   │   │   └── export/
│   │   │   │   └── analytics.module.ts
│   │   │   │
│   │   │   ├── marketplace/       # Marketplace Module
│   │   │   │   ├── models/
│   │   │   │   ├── services/
│   │   │   │   ├── components/
│   │   │   │   │   ├── module-list/
│   │   │   │   │   ├── module-detail/
│   │   │   │   │   ├── installed-modules/
│   │   │   │   │   └── module-store/
│   │   │   │   └── marketplace.module.ts
│   │   │   │
│   │   │   ├── users/             # User Management
│   │   │   │   └── ...
│   │   │   │
│   │   │   ├── settings/          # Settings Module
│   │   │   │   └── ...
│   │   │   │
│   │   │   └── notifications/     # Notifications Module
│   │   │       └── ...
│   │   │
│   │   ├── app.component.ts
│   │   ├── app.config.ts
│   │   └── app.routes.ts
│   │
│   ├── assets/
│   │   ├── i18n/                  # Translations
│   │   │   ├── en.json
│   │   │   ├── ru.json
│   │   │   ├── uz.json
│   │   │   ├── kk.json
│   │   │   └── ar.json
│   │   ├── images/
│   │   └── styles/
│   │
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   │
│   ├── index.html
│   ├── main.ts
│   └── styles.scss
│
├── angular.json
├── tsconfig.json
├── tsconfig.app.json
├── package.json
└── README.md
```

## 📦 Module Structure

### 1. Ticket Management Module

**Features:**
- Ticket list with filtering, sorting, pagination
- Ticket detail view with comments and history
- Create/edit ticket form
- SLA tracking
- Ticket assignment and transfer
- Bulk operations
- Export to CSV/PDF

**API Endpoints:**
```typescript
GET    /api/v1/tickets              // List tickets
POST   /api/v1/tickets              // Create ticket
GET    /api/v1/tickets/{id}         // Get ticket details
PUT    /api/v1/tickets/{id}         // Update ticket
DELETE /api/v1/tickets/{id}         // Delete ticket
POST   /api/v1/tickets/{id}/comments // Add comment
GET    /api/v1/tickets/{id}/history  // Get ticket history
PUT    /api/v1/tickets/{id}/assign   // Assign ticket
```

**Components:**
- `TicketListComponent` - Data table with filters
- `TicketDetailComponent` - Full ticket view
- `TicketFormComponent` - Create/edit form
- `TicketCommentsComponent` - Comment thread
- `TicketHistoryComponent` - Audit log

### 2. Channel Management Module

**Features:**
- Email channel configuration
- Telegram bot integration
- WhatsApp Business integration
- Live chat interface
- Widget configuration
- Message templates

**API Endpoints:**
```typescript
GET    /api/v1/channels            // List channels
POST   /api/v1/channels/email      // Configure email
POST   /api/v1/channels/telegram   // Configure Telegram
POST   /api/v1/channels/whatsapp   // Configure WhatsApp
GET    /api/v1/channels/messages   // Get messages
POST   /api/v1/channels/messages   // Send message
```

### 3. Knowledge Base Module

**Features:**
- Article creation with rich text editor
- Category tree management
- Full-text search
- Article versioning
- Public/private articles
- Related articles
- Article rating

**API Endpoints:**
```typescript
GET    /api/v1/knowledge/articles       // List articles
POST   /api/v1/knowledge/articles       // Create article
GET    /api/v1/knowledge/articles/{id}  // Get article
PUT    /api/v1/knowledge/articles/{id}  // Update article
DELETE /api/v1/knowledge/articles/{id}  // Delete article
GET    /api/v1/knowledge/search         // Search articles
GET    /api/v1/knowledge/categories     // List categories
```

### 4. AI Module

**Features:**
- AI chat interface
- Response suggestions
- Sentiment analysis
- Auto-categorization
- Smart search
- RAG integration

**API Endpoints:**
```typescript
POST   /api/v1/ai/chat                // AI chat
POST   /api/v1/ai/suggestions         // Get suggestions
POST   /api/v1/ai/sentiment           // Sentiment analysis
POST   /api/v1/ai/categorize          // Auto-categorize
POST   /api/v1/ai/search              // Semantic search
```

### 5. Analytics Module

**Features:**
- Dashboard with KPIs
- Custom reports
- Charts (line, bar, pie, doughnut)
- Date range filters
- Export reports
- Scheduled reports

**API Endpoints:**
```typescript
GET    /api/v1/analytics/dashboard     // Dashboard data
GET    /api/v1/analytics/reports       // List reports
POST   /api/v1/analytics/reports       // Create report
GET    /api/v1/analytics/reports/{id}  // Get report
GET    /api/v1/analytics/export        // Export report
```

### 6. Marketplace Module

**Features:**
- Module browser
- Module installation
- Module configuration
- Installed modules list
- Module updates
- Module ratings

**API Endpoints:**
```typescript
GET    /api/v1/marketplace/modules         // Browse modules
GET    /api/v1/marketplace/modules/{id}    // Module details
POST   /api/v1/marketplace/install/{id}    // Install module
DELETE /api/v1/marketplace/uninstall/{id}  // Uninstall module
GET    /api/v1/marketplace/installed       // Installed modules
```

## 🔌 API Integration

### API Service

```typescript
// src/app/core/services/api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(endpoint: string, params?: any): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${endpoint}`, { params });
  }

  post<T>(endpoint: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, body);
  }

  put<T>(endpoint: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, body);
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`);
  }
}
```

### Auth Interceptor

```typescript
// src/app/core/auth/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('access_token');

  if (token) {
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }

  return next(req);
};
```

### WebSocket Service

```typescript
// src/app/core/services/websocket.service.ts
import { Injectable } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private socket: Socket;

  constructor() {
    this.socket = io(environment.wsUrl, {
      auth: { token: localStorage.getItem('access_token') }
    });
  }

  listen<T>(event: string): Observable<T> {
    return new Observable(observer => {
      this.socket.on(event, (data: T) => observer.next(data));
    });
  }

  emit(event: string, data: any): void {
    this.socket.emit(event, data);
  }
}
```

## 📝 Implementation Roadmap

### Phase 1: Foundation (Week 1-2)

1. **Project Setup**
   - ✅ Create Angular project structure
   - ✅ Configure Angular Material
   - ✅ Set up routing
   - ✅ Create core module
   - ✅ Create shared module

2. **Authentication**
   - ✅ Login component
   - ✅ Register component
   - ✅ Auth service
   - ✅ Auth guard
   - ✅ Auth interceptor

3. **Layout**
   - ✅ Header component
   - ✅ Sidebar navigation
   - ✅ Footer component
   - ✅ Responsive design

### Phase 2: Core Modules (Week 3-4)

4. **Dashboard**
   - ✅ Dashboard layout
   - ✅ Widgets (stats, charts)
   - ✅ Recent activity
   - ✅ Quick actions

5. **Ticket Management**
   - ✅ Ticket list with filters
   - ✅ Ticket detail view
   - ✅ Create/edit ticket
   - ✅ Comments section
   - ✅ File attachments

6. **User Management**
   - ✅ User list
   - ✅ User profile
   - ✅ Role management
   - ✅ Team management

### Phase 3: Advanced Modules (Week 5-6)

7. **Channel Management**
   - ✅ Email configuration
   - ✅ Telegram integration
   - ✅ WhatsApp integration
   - ✅ Live chat interface

8. **Knowledge Base**
   - ✅ Article editor
   - ✅ Category management
   - ✅ Search interface
   - ✅ Article viewer

9. **AI Features**
   - ✅ AI chat component
   - ✅ Suggestions UI
   - ✅ Sentiment display

### Phase 4: Analytics & Extras (Week 7-8)

10. **Analytics Dashboard**
    - ✅ Charts integration
    - ✅ Custom reports
    - ✅ Export functionality

11. **Marketplace**
    - ✅ Module browser
    - ✅ Install/uninstall
    - ✅ Module configuration

12. **Settings & Configuration**
    - ✅ System settings
    - ✅ Notification preferences
    - ✅ Integration settings

## 💻 Development Guide

### Running Development Server

```bash
# Start backend
cd backend
./scripts/build-production.sh
java -jar build/artifacts/servicedesk-monolith-latest.jar

# Start frontend (in another terminal)
cd frontend
npm start

# Access
# Frontend: http://localhost:4200
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Environment Configuration

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  wsUrl: 'http://localhost:8080',
};

// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com/api/v1',
  wsUrl: 'wss://api.yourdomain.com',
};
```

### Building for Production

```bash
# Build with production configuration
npm run build:prod

# Output directory
dist/servicedesk-frontend/

# Deploy to web server
# Copy contents of dist/ to your web server (nginx, Apache, etc.)
```

### Docker Deployment

```dockerfile
# Dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:prod

FROM nginx:alpine
COPY --from=build /app/dist/servicedesk-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🎨 UI/UX Guidelines

### Material Design

- Use Angular Material components
- Follow Material Design guidelines
- Consistent spacing and typography
- Responsive layouts (mobile-first)

### Color Scheme

```scss
$primary: #1976d2;
$accent: #ff4081;
$warn: #f44336;
$success: #4caf50;
$info: #2196f3;
```

### Accessibility

- ARIA labels
- Keyboard navigation
- Screen reader support
- High contrast mode
- Focus indicators

## 🧪 Testing

```bash
# Run unit tests
npm test

# Run e2e tests
npm run e2e

# Code coverage
npm run test:coverage
```

## 📚 Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [Angular Material](https://material.angular.io/)
- [RxJS](https://rxjs.dev/)
- [Backend API Documentation](http://localhost:8080/swagger-ui.html)

## 🤝 Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/qahhor/FREE-SERICE-DESK/issues)
- **Email**: support@greenwhite.uz

---

**Ready to build! Start with `npm install && npm start`** 🚀
