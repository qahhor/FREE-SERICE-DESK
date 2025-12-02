# =================================================================
# ServiceDesk Platform - Makefile
# Convenience commands for development and deployment
# =================================================================

.PHONY: help build up down restart logs backup restore ssl-init ssl-renew \
        monitoring-up monitoring-down clean status shell test security-check \
        secrets dev prod

# Default compose file
COMPOSE_FILE ?= docker-compose.prod.yml
COMPOSE = docker compose -f $(COMPOSE_FILE)

# Colors for output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m

# =================================================================
# HELP
# =================================================================
help: ## Показать эту справку
	@echo ""
	@echo "$(BLUE)ServiceDesk Platform - Доступные команды$(NC)"
	@echo "==========================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

# =================================================================
# BUILD
# =================================================================
build: ## Собрать Docker образы
	@echo "$(BLUE)Building Docker images...$(NC)"
	$(COMPOSE) build
	@echo "$(GREEN)✓ Build completed$(NC)"

build-no-cache: ## Собрать образы без кэша
	@echo "$(BLUE)Building Docker images (no cache)...$(NC)"
	$(COMPOSE) build --no-cache
	@echo "$(GREEN)✓ Build completed$(NC)"

# =================================================================
# START/STOP
# =================================================================
up: ## Запустить все сервисы
	@echo "$(BLUE)Starting all services...$(NC)"
	$(COMPOSE) up -d
	@echo "$(GREEN)✓ Services started$(NC)"
	@echo "$(YELLOW)Run 'make logs' to view logs$(NC)"

down: ## Остановить все сервисы
	@echo "$(BLUE)Stopping all services...$(NC)"
	$(COMPOSE) down
	@echo "$(GREEN)✓ Services stopped$(NC)"

restart: ## Перезапустить все сервисы
	@echo "$(BLUE)Restarting all services...$(NC)"
	$(COMPOSE) restart
	@echo "$(GREEN)✓ Services restarted$(NC)"

stop: ## Остановить сервисы без удаления контейнеров
	@echo "$(BLUE)Stopping services...$(NC)"
	$(COMPOSE) stop
	@echo "$(GREEN)✓ Services stopped$(NC)"

# =================================================================
# DEVELOPMENT
# =================================================================
dev: ## Запустить в режиме разработки
	@echo "$(BLUE)Starting in development mode...$(NC)"
	docker compose -f docker-compose.dev.yml up -d
	@echo "$(GREEN)✓ Development environment started$(NC)"

dev-down: ## Остановить среду разработки
	@echo "$(BLUE)Stopping development environment...$(NC)"
	docker compose -f docker-compose.dev.yml down
	@echo "$(GREEN)✓ Development environment stopped$(NC)"

# =================================================================
# PRODUCTION
# =================================================================
prod: ## Запустить в production режиме
	@echo "$(BLUE)Starting in production mode...$(NC)"
	docker compose -f docker-compose.prod.yml up -d
	@echo "$(GREEN)✓ Production environment started$(NC)"

prod-down: ## Остановить production среду
	@echo "$(BLUE)Stopping production environment...$(NC)"
	docker compose -f docker-compose.prod.yml down
	@echo "$(GREEN)✓ Production environment stopped$(NC)"

# =================================================================
# LOGS
# =================================================================
logs: ## Показать логи всех сервисов
	$(COMPOSE) logs -f

logs-app: ## Показать логи приложения
	$(COMPOSE) logs -f servicedesk-monolith

logs-nginx: ## Показать логи Nginx
	$(COMPOSE) logs -f nginx

logs-db: ## Показать логи базы данных
	$(COMPOSE) logs -f postgres

# =================================================================
# STATUS
# =================================================================
status: ## Показать статус сервисов
	@echo "$(BLUE)Service Status:$(NC)"
	$(COMPOSE) ps
	@echo ""
	@echo "$(BLUE)Health Check:$(NC)"
	@curl -s http://localhost:8080/actuator/health 2>/dev/null | jq -r '.status' || echo "Application not responding"

stats: ## Показать использование ресурсов
	docker stats --no-stream

# =================================================================
# BACKUP
# =================================================================
backup: ## Создать бэкап базы данных
	@echo "$(BLUE)Creating database backup...$(NC)"
	./scripts/backup-postgres.sh
	@echo "$(GREEN)✓ Backup completed$(NC)"

restore: ## Восстановить из бэкапа (требуется BACKUP_FILE)
	@if [ -z "$(BACKUP_FILE)" ]; then \
		echo "$(RED)Error: BACKUP_FILE is required$(NC)"; \
		echo "Usage: make restore BACKUP_FILE=backups/servicedesk_2024-01-15_020000.sql.gz"; \
		exit 1; \
	fi
	@echo "$(BLUE)Restoring from backup...$(NC)"
	./scripts/restore-postgres.sh $(BACKUP_FILE)
	@echo "$(GREEN)✓ Restore completed$(NC)"

list-backups: ## Показать список бэкапов
	@echo "$(BLUE)Available backups:$(NC)"
	@ls -lh backups/*.sql.gz 2>/dev/null || echo "No backups found"

# =================================================================
# SSL
# =================================================================
ssl-init: ## Получить SSL сертификаты Let's Encrypt
	@if [ -z "$(DOMAIN)" ] || [ -z "$(EMAIL)" ]; then \
		echo "$(RED)Error: DOMAIN and EMAIL are required$(NC)"; \
		echo "Usage: make ssl-init DOMAIN=example.com EMAIL=admin@example.com"; \
		exit 1; \
	fi
	@echo "$(BLUE)Initializing SSL certificates...$(NC)"
	DOMAIN=$(DOMAIN) EMAIL=$(EMAIL) ./scripts/init-letsencrypt.sh
	@echo "$(GREEN)✓ SSL certificates obtained$(NC)"

ssl-renew: ## Обновить SSL сертификаты
	@echo "$(BLUE)Renewing SSL certificates...$(NC)"
	./scripts/renew-ssl.sh
	@echo "$(GREEN)✓ SSL certificates renewed$(NC)"

# =================================================================
# MONITORING
# =================================================================
monitoring-up: ## Запустить только мониторинг
	@echo "$(BLUE)Starting monitoring stack...$(NC)"
	$(COMPOSE) up -d prometheus alertmanager grafana loki promtail
	@echo "$(GREEN)✓ Monitoring started$(NC)"
	@echo "$(YELLOW)Grafana: http://localhost:3000$(NC)"
	@echo "$(YELLOW)Prometheus: http://localhost:9090$(NC)"

monitoring-down: ## Остановить мониторинг
	@echo "$(BLUE)Stopping monitoring stack...$(NC)"
	$(COMPOSE) stop prometheus alertmanager grafana loki promtail
	@echo "$(GREEN)✓ Monitoring stopped$(NC)"

# =================================================================
# SECURITY
# =================================================================
secrets: ## Сгенерировать все секреты
	@echo "$(BLUE)Generating secrets...$(NC)"
	./scripts/generate-secrets.sh
	@echo "$(GREEN)✓ Secrets generated$(NC)"

security-check: ## Проверить безопасность конфигурации
	@echo "$(BLUE)Running security check...$(NC)"
	./scripts/security-check.sh

# =================================================================
# SHELL ACCESS
# =================================================================
shell-app: ## Открыть shell в контейнере приложения
	docker exec -it servicedesk-monolith /bin/sh

shell-db: ## Открыть psql в контейнере базы данных
	docker exec -it servicedesk-postgres psql -U servicedesk -d servicedesk

shell-redis: ## Открыть redis-cli
	docker exec -it servicedesk-redis redis-cli

# =================================================================
# TESTING
# =================================================================
test: ## Запустить тесты
	@echo "$(BLUE)Running tests...$(NC)"
	cd backend && mvn test
	@echo "$(GREEN)✓ Tests completed$(NC)"

test-integration: ## Запустить интеграционные тесты
	@echo "$(BLUE)Running integration tests...$(NC)"
	cd backend && mvn verify -Pintegration-tests
	@echo "$(GREEN)✓ Integration tests completed$(NC)"

# =================================================================
# CLEANUP
# =================================================================
clean: ## Удалить контейнеры и volumes
	@echo "$(YELLOW)WARNING: This will delete all data!$(NC)"
	@read -p "Are you sure? (yes/no): " confirm && [ "$$confirm" = "yes" ]
	@echo "$(BLUE)Cleaning up...$(NC)"
	$(COMPOSE) down -v --remove-orphans
	@echo "$(GREEN)✓ Cleanup completed$(NC)"

clean-images: ## Удалить неиспользуемые Docker образы
	@echo "$(BLUE)Removing unused images...$(NC)"
	docker image prune -a -f
	@echo "$(GREEN)✓ Images cleaned$(NC)"

clean-all: ## Полная очистка Docker (ОСТОРОЖНО!)
	@echo "$(RED)WARNING: This will delete ALL Docker data!$(NC)"
	@read -p "Are you sure? (yes/no): " confirm && [ "$$confirm" = "yes" ]
	docker system prune -a -f --volumes
	@echo "$(GREEN)✓ Full cleanup completed$(NC)"

# =================================================================
# UTILITIES
# =================================================================
pull: ## Загрузить последние образы
	@echo "$(BLUE)Pulling latest images...$(NC)"
	$(COMPOSE) pull
	@echo "$(GREEN)✓ Images updated$(NC)"

config: ## Проверить docker-compose конфигурацию
	$(COMPOSE) config

version: ## Показать версии компонентов
	@echo "$(BLUE)Component versions:$(NC)"
	@echo "Docker: $$(docker --version)"
	@echo "Docker Compose: $$(docker compose version)"
	@echo "Make: $$(make --version | head -1)"

# =================================================================
# QUICK COMMANDS
# =================================================================
quick-start: secrets build up ## Быстрый старт (генерация секретов, сборка, запуск)
	@echo "$(GREEN)✓ Quick start completed$(NC)"
	@echo "$(YELLOW)Application: http://localhost:8080$(NC)"
	@echo "$(YELLOW)Grafana: http://localhost:3000$(NC)"

update: down pull build up ## Обновить и перезапустить
	@echo "$(GREEN)✓ Update completed$(NC)"
