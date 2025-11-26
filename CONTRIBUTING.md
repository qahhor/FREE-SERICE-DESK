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
- IDE with Lombok support

### Setup Steps

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/servicedesk-platform.git
cd servicedesk-platform

# Start infrastructure
docker-compose -f docker-compose.dev.yml up -d

# Build backend
cd backend
mvn clean install

# Run the application
cd ticket-service
mvn spring-boot:run
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

## Project Structure

```
backend/
├── common-lib/           # Shared code
│   ├── entity/          # Base entities
│   ├── dto/             # Common DTOs
│   ├── exception/       # Exception classes
│   ├── security/        # Security utilities
│   └── config/          # Common configs
│
└── ticket-service/      # Main service
    ├── entity/          # JPA entities
    ├── dto/             # Request/Response DTOs
    ├── repository/      # Data access
    ├── service/         # Business logic
    ├── controller/      # REST controllers
    └── config/          # Service configs
```

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
