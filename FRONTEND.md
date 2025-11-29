# Frontend Architecture

## 📢 Important Notice

The frontend directory has been **removed** from this repository as of the monolithic architecture transformation.

## 🤔 Why was Frontend removed?

### Reasons:

1. **Backend-First Approach**
   - This repository focuses on the **backend monolithic application**
   - Frontend was not integrated with the backend
   - No static resources were served by the monolith

2. **Separation of Concerns**
   - Modern architecture pattern: separate frontend and backend repositories
   - Independent deployment and scaling
   - Different technology stacks and release cycles

3. **Repository Clarity**
   - Frontend applications (Angular) were stub/template implementations
   - No production-ready code
   - Dependencies were not installed (no node_modules)

4. **Best Practices**
   - Microservices/monolith backend ↔ Multiple frontend clients pattern
   - Single API, multiple consumers (web, mobile, desktop)
   - Frontend applications can be developed independently

## 🎯 Frontend Options

You have several options for building the frontend:

### Option 1: Separate Repository (Recommended)

Create a separate Git repository for frontend applications:

```
servicedesk-frontend/           # New repository
├── agent-app/                  # Agent dashboard (Angular/React/Vue)
├── customer-portal/            # Customer self-service portal
├── admin-panel/                # Admin configuration panel
└── widget/                     # Embeddable chat widget
```

**Benefits:**
- Independent versioning and deployment
- Separate CI/CD pipelines
- Different teams can work independently
- Technology flexibility (Angular, React, Vue, etc.)

### Option 2: Use API Directly

The ServiceDesk monolith provides a **comprehensive REST API**:

**API Documentation:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

**Key Endpoints:**
```
Authentication:
  POST /api/v1/auth/login
  POST /api/v1/auth/register
  POST /api/v1/auth/refresh

Tickets:
  GET    /api/v1/tickets
  POST   /api/v1/tickets
  GET    /api/v1/tickets/{id}
  PUT    /api/v1/tickets/{id}
  DELETE /api/v1/tickets/{id}

Users:
  GET    /api/v1/users
  POST   /api/v1/users
  GET    /api/v1/users/{id}
  PUT    /api/v1/users/{id}

Channels:
  GET    /api/v1/channels
  POST   /api/v1/channels/email
  POST   /api/v1/channels/telegram
  POST   /api/v1/channels/whatsapp

Knowledge Base:
  GET    /api/v1/knowledge/articles
  POST   /api/v1/knowledge/articles
  GET    /api/v1/knowledge/search?q=query

AI:
  POST   /api/v1/ai/chat
  POST   /api/v1/ai/suggestions
  POST   /api/v1/ai/sentiment

Analytics:
  GET    /api/v1/analytics/dashboard
  GET    /api/v1/analytics/reports
```

### Option 3: Use Third-Party Tools

Integrate with existing tools:
- **Admin Panel**: [AdminLTE](https://adminlte.io/), [Tabler](https://tabler.io/)
- **Helpdesk UI**: [Chatwoot](https://www.chatwoot.com/) (open source)
- **Customer Portal**: WordPress plugins, custom HTML/CSS
- **Mobile Apps**: React Native, Flutter

### Option 4: Rebuild Frontend (If Needed)

If you need the original Angular stubs, they can be found in the git history:

```bash
# Find the commit before frontend removal
git log --all --oneline -- frontend/

# Checkout frontend from specific commit
git checkout <commit-hash> -- frontend/
```

## 🏗️ Frontend Architecture Recommendations

### Recommended Stack

**Agent Dashboard:**
- Framework: React + TypeScript or Vue 3 + TypeScript
- UI Library: Material-UI, Ant Design, or Tailwind CSS
- State: Redux Toolkit, Zustand, or Pinia
- API Client: Axios with interceptors for JWT
- Real-time: WebSocket or Socket.io

**Customer Portal:**
- Framework: Next.js (SSR for SEO) or Nuxt.js
- Styling: Tailwind CSS
- Authentication: NextAuth.js or custom JWT

**Embeddable Widget:**
- Vanilla JavaScript or Preact (minimal bundle size)
- Shadow DOM for style isolation
- WebSocket for real-time chat

### Project Structure Example

```
servicedesk-frontend/
├── packages/
│   ├── agent-app/              # Agent dashboard
│   │   ├── src/
│   │   │   ├── components/
│   │   │   ├── pages/
│   │   │   ├── services/       # API calls
│   │   │   ├── store/          # State management
│   │   │   └── App.tsx
│   │   └── package.json
│   │
│   ├── customer-portal/        # Customer portal
│   │   ├── src/
│   │   └── package.json
│   │
│   ├── shared-lib/             # Shared components/utils
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── types/             # TypeScript types from API
│   │   └── api-client/        # Shared API client
│   │
│   └── widget/                 # Chat widget
│       ├── src/
│       └── package.json
│
├── package.json                # Monorepo root
└── pnpm-workspace.yaml         # Or yarn workspaces
```

## 🔌 Integration with Backend

### Authentication Flow

```typescript
// Login
const response = await fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { accessToken, refreshToken } = await response.json();

// Store tokens
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);

// Use token in subsequent requests
const tickets = await fetch('http://localhost:8080/api/v1/tickets', {
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});
```

### WebSocket Connection

```typescript
import { io } from 'socket.io-client';

const socket = io('http://localhost:8080', {
  auth: { token: accessToken },
  transports: ['websocket']
});

// Listen for new tickets
socket.on('ticket:created', (ticket) => {
  console.log('New ticket:', ticket);
});

// Listen for updates
socket.on('ticket:updated', (ticket) => {
  console.log('Ticket updated:', ticket);
});
```

## 📚 API Documentation

Full API documentation is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

## 🚀 Getting Started with Frontend Development

### 1. Set up Backend

```bash
# Start backend monolith
cd servicedesk-platform
docker-compose -f docker-compose.monolith.yml up -d

# Verify API is running
curl http://localhost:8080/actuator/health
```

### 2. Create Frontend Project

```bash
# Create React app
npx create-react-app servicedesk-frontend --template typescript

# Or Vue
npm create vue@latest servicedesk-frontend

# Or Next.js
npx create-next-app@latest servicedesk-frontend --typescript
```

### 3. Configure API Client

```typescript
// src/api/client.ts
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token refresh on 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### 4. Start Development

```bash
cd servicedesk-frontend
npm install
npm start

# Access at http://localhost:3000
```

## 🔐 Security Considerations

### CORS Configuration

Backend already supports CORS. If you encounter issues:

```yaml
# backend/monolith-app/src/main/resources/application.yml
cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:4200
    - https://yourdomain.com
  allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```

### JWT Token Management

- Store tokens in `localStorage` or `sessionStorage`
- Implement token refresh mechanism
- Clear tokens on logout
- Handle expired tokens gracefully

## 📞 Support & Questions

If you have questions about:
- **Backend API**: See [README.md](README.md) and [API Reference](#api-documentation)
- **Frontend Development**: Create a new issue in the repository
- **Integration**: Check Swagger UI for endpoint details

## 📝 Summary

- ✅ Backend monolith provides complete REST API
- ✅ Frontend can be developed separately
- ✅ Multiple frontend options available
- ✅ API documentation available via Swagger
- ✅ WebSocket support for real-time features
- ✅ JWT authentication implemented

**Next Steps:**
1. Explore the API via Swagger UI
2. Choose your frontend framework
3. Create separate frontend repository
4. Integrate with backend API
5. Deploy independently

---

**Questions?** Open an issue or contact: support@greenwhite.uz
