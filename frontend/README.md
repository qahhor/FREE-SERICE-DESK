# ServiceDesk Frontend

Modern Angular 17 frontend application for the ServiceDesk Platform.

## 🚀 Features

- **Modern Stack**: Built with Angular 17 (Standalone Components), TypeScript, Angular Material
- **Comprehensive Modules**:
  - 🔐 Authentication (Login/Register)
  - 📊 Dashboard with real-time stats
  - 🎫 Ticket Management (List, Detail, Create)
  - 💬 Channel Management (Email, Telegram, WhatsApp, Live Chat)
  - 📚 Knowledge Base
  - 🤖 AI Assistant
  - 📈 Analytics & Reports
  - 🛍️ Marketplace for plugins/integrations
  - ⚙️ Settings

- **Advanced Features**:
  - JWT Authentication with automatic token refresh
  - HTTP Interceptors for auth and error handling
  - WebSocket support for real-time updates
  - Lazy-loaded routes for optimal performance
  - Responsive Material Design UI
  - Custom pipes and directives
  - Reusable component library

## 📁 Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                    # Core functionality
│   │   │   ├── guards/              # Route guards
│   │   │   ├── interceptors/        # HTTP interceptors
│   │   │   ├── models/              # Data models
│   │   │   └── services/            # Core services
│   │   │
│   │   ├── shared/                  # Shared resources
│   │   │   ├── components/          # Reusable components
│   │   │   ├── pipes/               # Custom pipes
│   │   │   └── directives/          # Custom directives
│   │   │
│   │   ├── features/                # Feature modules
│   │   │   ├── auth/                # Authentication
│   │   │   ├── dashboard/           # Main dashboard
│   │   │   ├── tickets/             # Ticket management
│   │   │   ├── channels/            # Channel management
│   │   │   ├── knowledge/           # Knowledge base
│   │   │   ├── ai/                  # AI features
│   │   │   ├── analytics/           # Analytics & reports
│   │   │   ├── marketplace/         # Plugins marketplace
│   │   │   └── settings/            # User settings
│   │   │
│   │   ├── app.component.ts         # Root component
│   │   ├── app.config.ts            # App configuration
│   │   └── app.routes.ts            # Route definitions
│   │
│   ├── environments/                # Environment configs
│   ├── assets/                      # Static assets
│   ├── index.html                   # Main HTML
│   ├── main.ts                      # Bootstrap file
│   └── styles.scss                  # Global styles
│
├── angular.json                     # Angular CLI config
├── tsconfig.json                    # TypeScript config
├── package.json                     # Dependencies
└── README.md                        # This file
```

## 🛠️ Prerequisites

- Node.js 18+ and npm
- Angular CLI 17+
- Backend API running on `http://localhost:8080`

## 📦 Installation

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Configure environment:**

   The application is pre-configured to use `http://localhost:8080` for development.

   To change the API URL, edit `src/environments/environment.ts`:
   ```typescript
   export const environment = {
     apiUrl: 'http://your-api-url/api/v1',
     wsUrl: 'http://your-api-url',
     // ...
   };
   ```

## 🚀 Development

**Start development server:**
```bash
npm start
# or
ng serve
```

Application will be available at `http://localhost:4200`

**The dev server includes:**
- Hot reload
- API proxy to backend (configured in `proxy.conf.json`)
- Source maps for debugging

## 🏗️ Building

**Development build:**
```bash
npm run build
```

**Production build:**
```bash
npm run build:prod
# or
ng build --configuration production
```

**Output:**
- Built files in `dist/servicedesk-frontend/`
- Optimized and minified for production
- Ready for deployment

## 🧪 Testing

**Run unit tests:**
```bash
npm test
# or
ng test
```

**Run end-to-end tests:**
```bash
npm run e2e
# or
ng e2e
```

## 📝 Key Technologies

### Core
- **Angular 17**: Latest Angular with standalone components
- **TypeScript 5**: Strongly-typed JavaScript
- **RxJS 7**: Reactive programming
- **Angular Material**: Material Design components

### Features
- **Chart.js**: Charts and visualizations
- **Socket.io Client**: Real-time WebSocket connections
- **Angular Router**: Client-side routing with lazy loading

### Development Tools
- **Angular CLI**: Project scaffolding and build tools
- **Webpack**: Module bundler (via Angular CLI)
- **TSLint/ESLint**: Code linting
- **Prettier**: Code formatting

## 🔌 API Integration

The frontend connects to the backend API at `/api/v1/*` endpoints:

### Authentication
```typescript
POST /api/v1/auth/login       // Login
POST /api/v1/auth/register    // Register
POST /api/v1/auth/refresh     // Refresh token
```

### Tickets
```typescript
GET    /api/v1/tickets           // List tickets
POST   /api/v1/tickets           // Create ticket
GET    /api/v1/tickets/:id       // Get ticket
PUT    /api/v1/tickets/:id       // Update ticket
DELETE /api/v1/tickets/:id       // Delete ticket
POST   /api/v1/tickets/:id/close // Close ticket
```

### Other Modules
- Channels: `/api/v1/channels/*`
- Knowledge: `/api/v1/knowledge/*`
- AI: `/api/v1/ai/*`
- Analytics: `/api/v1/analytics/*`
- Marketplace: `/api/v1/marketplace/*`

Full API documentation: http://localhost:8080/swagger-ui.html

## 🔐 Authentication Flow

1. User logs in via `/auth/login`
2. Backend returns JWT access token and refresh token
3. Access token stored in localStorage
4. HTTP interceptor adds `Authorization: Bearer <token>` header to all requests
5. On 401 error, interceptor attempts token refresh
6. If refresh fails, user is redirected to login page

## 🎨 UI Components

### Shared Components
- **LoadingSpinner**: Loading indicator
- **ConfirmationDialog**: Confirmation dialogs
- **EmptyState**: Empty state placeholder
- **Layout**: Main app layout with sidebar

### Custom Pipes
- **truncate**: Truncate text with ellipsis
- **timeAgo**: Convert dates to relative time (e.g., "2 hours ago")
- **safeHtml**: Sanitize HTML content

### Directives
- **autoFocus**: Auto-focus input on load

## 🌐 Internationalization (i18n)

The app is prepared for multi-language support:
- Supported languages: English, Russian, Uzbek
- Default language: English
- Language configuration in `environment.ts`

## 🚢 Deployment

### Static Hosting (Nginx, Apache, etc.)

1. Build production bundle:
   ```bash
   npm run build:prod
   ```

2. Copy `dist/servicedesk-frontend/*` to web server

3. Configure server to serve `index.html` for all routes:

   **Nginx example:**
   ```nginx
   server {
     listen 80;
     server_name yourdomain.com;
     root /var/www/servicedesk-frontend;

     location / {
       try_files $uri $uri/ /index.html;
     }

     location /api/ {
       proxy_pass http://localhost:8080;
     }
   }
   ```

### Docker

Create `Dockerfile` in frontend directory:
```dockerfile
# Build stage
FROM node:18 as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build:prod

# Production stage
FROM nginx:alpine
COPY --from=build /app/dist/servicedesk-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build and run:
```bash
docker build -t servicedesk-frontend .
docker run -p 80:80 servicedesk-frontend
```

### Cloud Platforms

- **Vercel/Netlify**: Connect Git repo and auto-deploy
- **AWS S3 + CloudFront**: Host as static website
- **Azure Static Web Apps**: Deploy with GitHub Actions
- **Google Cloud Storage**: Host as static site

## 🐛 Troubleshooting

### Common Issues

**1. API Connection Failed**
- Ensure backend is running on `http://localhost:8080`
- Check `proxy.conf.json` configuration
- Verify CORS is enabled on backend

**2. Module Not Found**
- Run `npm install` to install dependencies
- Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`

**3. Build Errors**
- Check TypeScript version compatibility
- Clear Angular cache: `rm -rf .angular/cache`
- Update Angular CLI: `npm install -g @angular/cli@latest`

**4. WebSocket Connection Failed**
- Ensure Socket.io is configured on backend
- Check `wsUrl` in environment configuration
- Verify JWT token is valid

## 📚 Documentation

- [Angular Documentation](https://angular.dev/)
- [Angular Material](https://material.angular.io/)
- [RxJS](https://rxjs.dev/)
- [TypeScript](https://www.typescriptlang.org/)
- [Backend API Documentation](http://localhost:8080/swagger-ui.html)

## 🤝 Contributing

1. Follow Angular style guide
2. Use TypeScript strict mode
3. Write unit tests for new features
4. Follow commit message conventions
5. Update documentation

## 📄 License

Copyright © 2024 ServiceDesk Platform

---

**Questions?** Contact: support@greenwhite.uz
