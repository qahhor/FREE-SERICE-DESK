# Развёртывание ServiceDesk Platform

Пошаговое руководство по развёртыванию ServiceDesk Platform в production среде.

## Содержание

1. [Требования](#требования)
2. [Подготовка сервера](#подготовка-сервера)
3. [Установка Docker](#установка-docker)
4. [Клонирование репозитория](#клонирование-репозитория)
5. [Настройка окружения](#настройка-окружения)
6. [Получение SSL сертификатов](#получение-ssl-сертификатов)
7. [Запуск приложения](#запуск-приложения)
8. [Проверка работоспособности](#проверка-работоспособности)
9. [Настройка автозапуска](#настройка-автозапуска)

---

## Требования

### Минимальные системные требования

| Компонент | Минимум | Рекомендуемо |
|-----------|---------|--------------|
| CPU | 2 cores | 4+ cores |
| RAM | 4 GB | 8+ GB |
| Disk | 20 GB SSD | 50+ GB SSD |
| OS | Ubuntu 20.04 | Ubuntu 22.04 LTS |

### Программное обеспечение

- Docker 20.10+
- Docker Compose 2.0+
- Git
- OpenSSL

### Сетевые требования

- Открытые порты: 80 (HTTP), 443 (HTTPS)
- Доменное имя, направленное на сервер
- Статический IP-адрес (рекомендуется)

---

## Подготовка сервера

### 1. Обновление системы

```bash
sudo apt update && sudo apt upgrade -y
```

### 2. Установка необходимых пакетов

```bash
sudo apt install -y \
    curl \
    wget \
    git \
    openssl \
    ca-certificates \
    gnupg \
    lsb-release
```

### 3. Настройка файрвола

```bash
# Установка UFW (если не установлен)
sudo apt install -y ufw

# Разрешение SSH
sudo ufw allow OpenSSH

# Разрешение HTTP и HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Включение файрвола
sudo ufw enable
```

### 4. Настройка swap (если RAM < 4GB)

```bash
# Создание swap файла
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Добавление в fstab для постоянного использования
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

---

## Установка Docker

### 1. Установка Docker

```bash
# Добавление официального GPG ключа Docker
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Добавление репозитория
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Установка Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

### 2. Настройка Docker для текущего пользователя

```bash
# Добавление пользователя в группу docker
sudo usermod -aG docker $USER

# Применение изменений (или перелогиньтесь)
newgrp docker
```

### 3. Проверка установки

```bash
docker --version
docker compose version
```

---

## Клонирование репозитория

```bash
# Переход в директорию для приложений
cd /opt

# Клонирование репозитория
sudo git clone https://github.com/your-org/servicedesk.git
cd servicedesk

# Установка владельца
sudo chown -R $USER:$USER /opt/servicedesk
```

---

## Настройка окружения

### 1. Создание файла .env

```bash
# Копирование примера
cp .env.production.example .env

# Редактирование
nano .env
```

### 2. Обязательные настройки

```bash
# Генерация безопасного JWT секрета
JWT_SECRET=$(openssl rand -base64 48)
echo "JWT_SECRET=$JWT_SECRET"

# Генерация пароля для базы данных
DB_PASSWORD=$(openssl rand -base64 32)
echo "DB_PASSWORD=$DB_PASSWORD"

# Генерация пароля для Grafana
GRAFANA_PASSWORD=$(openssl rand -base64 16)
echo "GRAFANA_ADMIN_PASSWORD=$GRAFANA_PASSWORD"
```

### 3. Пример .env файла

```env
# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080

# Security
JWT_SECRET=your-generated-jwt-secret-here

# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=servicedesk
DB_USERNAME=servicedesk
DB_PASSWORD=your-generated-password-here

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Elasticsearch
ELASTICSEARCH_URIS=http://elasticsearch:9200

# Email (настройте под ваш SMTP сервер)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Monitoring
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=your-grafana-password

# Domain (для SSL)
DOMAIN=servicedesk.yourdomain.com
SSL_EMAIL=admin@yourdomain.com
```

### 4. Автоматическая генерация секретов

```bash
# Используйте скрипт для генерации всех секретов
chmod +x scripts/generate-secrets.sh
./scripts/generate-secrets.sh
```

---

## Получение SSL сертификатов

### Вариант 1: Let's Encrypt (рекомендуется)

```bash
# Установка переменных
export DOMAIN=servicedesk.yourdomain.com
export EMAIL=admin@yourdomain.com

# Запуск скрипта инициализации SSL
chmod +x scripts/init-letsencrypt.sh
./scripts/init-letsencrypt.sh
```

### Вариант 2: Собственные сертификаты

```bash
# Создание директории для сертификатов
mkdir -p nginx/ssl/live/servicedesk

# Копирование сертификатов
cp /path/to/your/fullchain.pem nginx/ssl/live/servicedesk/
cp /path/to/your/privkey.pem nginx/ssl/live/servicedesk/

# Генерация DH параметров
openssl dhparam -out nginx/ssl/dhparam.pem 2048
```

---

## Запуск приложения

### 1. Сборка образов (опционально)

```bash
# Сборка локальных образов
make build

# Или через docker compose
docker compose -f docker-compose.prod.yml build
```

### 2. Запуск всех сервисов

```bash
# Запуск в фоновом режиме
make up

# Или через docker compose
docker compose -f docker-compose.prod.yml up -d
```

### 3. Проверка статуса

```bash
# Просмотр статуса контейнеров
docker compose -f docker-compose.prod.yml ps

# Просмотр логов
make logs

# Логи конкретного сервиса
docker compose -f docker-compose.prod.yml logs -f servicedesk-monolith
```

---

## Проверка работоспособности

### 1. Health Check

```bash
# Проверка API
curl -k https://localhost/actuator/health

# Или через домен
curl https://servicedesk.yourdomain.com/actuator/health
```

### 2. Ожидаемый ответ

```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "elasticsearch": {"status": "UP"}
  }
}
```

### 3. Проверка веб-интерфейса

- Приложение: https://servicedesk.yourdomain.com
- Grafana: https://servicedesk.yourdomain.com:3000
- Prometheus: http://localhost:9090 (только локально)

---

## Настройка автозапуска

### Создание systemd сервиса

```bash
sudo tee /etc/systemd/system/servicedesk.service > /dev/null <<EOF
[Unit]
Description=ServiceDesk Platform
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/servicedesk
ExecStart=/usr/bin/docker compose -f docker-compose.prod.yml up -d
ExecStop=/usr/bin/docker compose -f docker-compose.prod.yml down
TimeoutStartSec=300

[Install]
WantedBy=multi-user.target
EOF

# Активация сервиса
sudo systemctl daemon-reload
sudo systemctl enable servicedesk
```

### Настройка автообновления SSL

```bash
# Добавление задачи в cron
(crontab -l 2>/dev/null; echo "0 0 * * * /opt/servicedesk/scripts/renew-ssl.sh >> /var/log/certbot-renew.log 2>&1") | crontab -
```

### Настройка автоматического бэкапа

```bash
# Добавление задачи бэкапа в cron (ежедневно в 2:00)
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/servicedesk/scripts/backup-postgres.sh >> /var/log/pg-backup.log 2>&1") | crontab -
```

---

## Обновление приложения

### 1. Получение обновлений

```bash
cd /opt/servicedesk
git pull origin main
```

### 2. Пересборка и перезапуск

```bash
# Остановка сервисов
make down

# Пересборка образов
make build

# Запуск обновлённой версии
make up
```

### 3. Откат при проблемах

```bash
# Откат к предыдущей версии
git checkout HEAD~1

# Пересборка и перезапуск
make down && make build && make up
```

---

## Устранение неполадок

При возникновении проблем обратитесь к [документации по устранению неполадок](TROUBLESHOOTING.md).

### Быстрые команды диагностики

```bash
# Просмотр логов всех сервисов
make logs

# Проверка использования ресурсов
docker stats

# Перезапуск конкретного сервиса
docker compose -f docker-compose.prod.yml restart servicedesk-monolith

# Полная перезагрузка
make down && make up
```

---

## Дополнительные ресурсы

- [Настройка SSL](SSL-SETUP.md)
- [Настройка мониторинга](MONITORING.md)
- [Бэкап и восстановление](BACKUP-RESTORE.md)
- [Устранение неполадок](TROUBLESHOOTING.md)
