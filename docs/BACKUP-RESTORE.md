# Резервное копирование и восстановление

Руководство по процедурам резервного копирования и восстановления данных ServiceDesk Platform.

## Содержание

1. [Обзор](#обзор)
2. [Настройка бэкапов](#настройка-бэкапов)
3. [Ручное создание бэкапа](#ручное-создание-бэкапа)
4. [Автоматические бэкапы](#автоматические-бэкапы)
5. [Восстановление из бэкапа](#восстановление-из-бэкапа)
6. [Хранение бэкапов в S3/MinIO](#хранение-бэкапов-в-s3minio)
7. [Проверка бэкапов](#проверка-бэкапов)
8. [Лучшие практики](#лучшие-практики)

---

## Обзор

### Что резервируется

| Компонент | Метод | Частота |
|-----------|-------|---------|
| PostgreSQL | pg_dump | Ежедневно |
| Redis | RDB snapshot | По требованию |
| Загруженные файлы | rsync/tar | Ежедневно |
| Конфигурации | git | При изменении |

### Структура директорий

```
/opt/servicedesk/
├── backups/
│   ├── servicedesk_2024-01-15_020000.sql.gz
│   ├── servicedesk_2024-01-15_020000.sql.gz.sha256
│   ├── servicedesk_2024-01-14_020000.sql.gz
│   └── ...
```

---

## Настройка бэкапов

### 1. Создание директории для бэкапов

```bash
mkdir -p /opt/servicedesk/backups
chmod 750 /opt/servicedesk/backups
```

### 2. Настройка прав на скрипты

```bash
chmod +x /opt/servicedesk/scripts/backup-postgres.sh
chmod +x /opt/servicedesk/scripts/restore-postgres.sh
```

### 3. Настройка переменных окружения

```bash
# В файле .env
BACKUP_DIR=/opt/servicedesk/backups
RETENTION_DAYS=7

# Для S3/MinIO (опционально)
S3_ENABLED=true
S3_BUCKET=servicedesk-backups
S3_ENDPOINT=https://s3.amazonaws.com
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
```

---

## Ручное создание бэкапа

### Базовая команда

```bash
# Через Makefile
make backup

# Или напрямую
./scripts/backup-postgres.sh
```

### С кастомными параметрами

```bash
# Указание директории для бэкапа
BACKUP_DIR=/mnt/backups ./scripts/backup-postgres.sh

# С загрузкой в S3
S3_ENABLED=true S3_BUCKET=my-bucket ./scripts/backup-postgres.sh
```

### Пример вывода

```
=====================================
ServiceDesk - PostgreSQL Backup
=====================================

[2024-01-15 02:00:00] INFO: Creating backup directory...
[2024-01-15 02:00:00] INFO: Starting database backup...
[2024-01-15 02:00:00] INFO: Database: servicedesk@postgres:5432
[2024-01-15 02:00:01] INFO: Using Docker container for backup...
[2024-01-15 02:00:15] INFO: Backup created successfully: servicedesk_2024-01-15_020000.sql.gz (125M)
[2024-01-15 02:00:15] INFO: Verifying backup integrity...
[2024-01-15 02:00:16] INFO: Backup integrity verified
[2024-01-15 02:00:16] INFO: Creating checksum...
[2024-01-15 02:00:16] INFO: Rotating old backups (keeping last 7 days)...
[2024-01-15 02:00:16] INFO: Local backups remaining: 7

=====================================
Backup Complete!
=====================================

Backup file:     servicedesk_2024-01-15_020000.sql.gz
Backup size:     125M
Backup location: /opt/servicedesk/backups
```

---

## Автоматические бэкапы

### Настройка через cron

```bash
# Открытие crontab
crontab -e

# Добавление задачи (ежедневно в 2:00)
0 2 * * * /opt/servicedesk/scripts/backup-postgres.sh >> /var/log/pg-backup.log 2>&1
```

### Проверка настройки cron

```bash
# Просмотр текущих задач
crontab -l

# Проверка логов
tail -f /var/log/pg-backup.log
```

### Мониторинг бэкапов

```bash
# Проверка последнего бэкапа
ls -la /opt/servicedesk/backups/ | tail -5

# Проверка размера бэкапов
du -sh /opt/servicedesk/backups/*
```

---

## Восстановление из бэкапа

### Список доступных бэкапов

```bash
./scripts/restore-postgres.sh --list
```

### Проверка бэкапа перед восстановлением

```bash
./scripts/restore-postgres.sh backups/servicedesk_2024-01-15_020000.sql.gz --verify
```

### Полное восстановление

```bash
# С подтверждением
./scripts/restore-postgres.sh backups/servicedesk_2024-01-15_020000.sql.gz

# Без подтверждения (для автоматизации)
./scripts/restore-postgres.sh backups/servicedesk_2024-01-15_020000.sql.gz --force

# С пересозданием базы данных
./scripts/restore-postgres.sh backups/servicedesk_2024-01-15_020000.sql.gz --drop-db
```

### Пример вывода восстановления

```
=====================================
ServiceDesk - PostgreSQL Restore
=====================================

[2024-01-15 10:30:00] INFO: Backup file: backups/servicedesk_2024-01-15_020000.sql.gz
[2024-01-15 10:30:00] INFO: File size: 125M
[2024-01-15 10:30:00] INFO: Verifying backup integrity...
[2024-01-15 10:30:01] INFO: Gzip integrity check passed
[2024-01-15 10:30:01] INFO: Verifying SHA256 checksum...
[2024-01-15 10:30:02] INFO: SHA256 checksum verified

WARNING: This will restore the database from backup.
All current data will be overwritten!

Database: servicedesk@postgres:5432
Backup:   servicedesk_2024-01-15_020000.sql.gz

Are you sure you want to continue? (yes/no): yes

[2024-01-15 10:30:10] INFO: Restoring database...
[2024-01-15 10:32:45] INFO: Database restored successfully

=====================================
Restore Complete!
=====================================
```

---

## Хранение бэкапов в S3/MinIO

### Настройка AWS S3

```bash
# Установка AWS CLI
sudo apt install -y awscli

# Конфигурация
aws configure
# AWS Access Key ID: your-access-key
# AWS Secret Access Key: your-secret-key
# Default region name: eu-west-1
# Default output format: json
```

### Настройка MinIO

```bash
# В файле .env
S3_ENABLED=true
S3_BUCKET=servicedesk-backups
S3_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

### Создание бакета

```bash
# Для MinIO
docker exec -it servicedesk-minio mc mb local/servicedesk-backups

# Для AWS S3
aws s3 mb s3://servicedesk-backups
```

### Загрузка бэкапа в S3

```bash
S3_ENABLED=true ./scripts/backup-postgres.sh
```

### Скачивание бэкапа из S3

```bash
# Из AWS S3
aws s3 cp s3://servicedesk-backups/2024/01/servicedesk_2024-01-15_020000.sql.gz ./

# Из MinIO
aws s3 cp --endpoint-url http://localhost:9000 s3://servicedesk-backups/2024/01/servicedesk_2024-01-15_020000.sql.gz ./
```

---

## Проверка бэкапов

### Автоматическая проверка целостности

```bash
# Проверка gzip
gzip -t backups/servicedesk_2024-01-15_020000.sql.gz && echo "OK"

# Проверка SHA256
sha256sum -c backups/servicedesk_2024-01-15_020000.sql.gz.sha256
```

### Тестовое восстановление

Рекомендуется регулярно проводить тестовое восстановление в отдельной среде:

```bash
# Создание тестового контейнера PostgreSQL
docker run -d --name postgres-test \
  -e POSTGRES_PASSWORD=test \
  -e POSTGRES_DB=servicedesk_test \
  postgres:16-alpine

# Восстановление в тестовую базу
gunzip -c backups/servicedesk_2024-01-15_020000.sql.gz | \
  docker exec -i postgres-test psql -U postgres -d servicedesk_test

# Проверка данных
docker exec postgres-test psql -U postgres -d servicedesk_test -c "SELECT COUNT(*) FROM tickets;"

# Удаление тестового контейнера
docker rm -f postgres-test
```

---

## Лучшие практики

### 1. Правило 3-2-1

- **3** копии данных
- **2** разных типа носителей (локально + облако)
- **1** копия вне офиса (off-site)

### 2. Регулярное тестирование

- Еженедельно проверяйте целостность бэкапов
- Ежемесячно проводите тестовое восстановление
- Документируйте результаты тестов

### 3. Мониторинг бэкапов

```bash
# Добавьте в систему мониторинга проверку:
# - Время последнего бэкапа
# - Размер бэкапа
# - Успешность выполнения

# Пример проверки для Prometheus (в alerts.yml):
# - alert: BackupMissing
#   expr: time() - backup_last_success_timestamp > 86400
#   for: 1h
#   labels:
#     severity: critical
#   annotations:
#     summary: "Database backup is missing"
```

### 4. Шифрование бэкапов

Для конфиденциальных данных рекомендуется шифрование:

```bash
# Шифрование бэкапа
gpg --symmetric --cipher-algo AES256 backups/servicedesk_2024-01-15_020000.sql.gz

# Расшифровка перед восстановлением
gpg --decrypt backups/servicedesk_2024-01-15_020000.sql.gz.gpg > restored.sql.gz
```

### 5. Ротация и очистка

```bash
# Настройка периода хранения в .env
RETENTION_DAYS=7      # Локальные бэкапы
S3_RETENTION_DAYS=30  # Облачные бэкапы
```

---

## Устранение неполадок

### Бэкап не создаётся

```bash
# Проверка подключения к базе данных
docker exec servicedesk-postgres pg_isready

# Проверка прав доступа
ls -la /opt/servicedesk/backups/

# Проверка свободного места
df -h /opt/servicedesk/backups/
```

### Ошибка восстановления

```bash
# Проверка логов PostgreSQL
docker logs servicedesk-postgres --tail 100

# Проверка целостности файла
gzip -t backup-file.sql.gz
```

### Медленное восстановление

```bash
# Временное отключение индексов
# Восстановление данных
# Пересоздание индексов

# Увеличение буферов PostgreSQL
docker exec -it servicedesk-postgres psql -U servicedesk -c "SET work_mem='256MB';"
```

---

## Связанная документация

- [Развёртывание](DEPLOYMENT.md)
- [Мониторинг](MONITORING.md)
- [Устранение неполадок](TROUBLESHOOTING.md)
