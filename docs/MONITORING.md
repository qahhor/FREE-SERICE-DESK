# Мониторинг ServiceDesk Platform

Руководство по настройке и использованию системы мониторинга.

## Содержание

1. [Обзор архитектуры](#обзор-архитектуры)
2. [Компоненты мониторинга](#компоненты-мониторинга)
3. [Настройка Prometheus](#настройка-prometheus)
4. [Настройка Grafana](#настройка-grafana)
5. [Настройка алертов](#настройка-алертов)
6. [Логирование с Loki](#логирование-с-loki)
7. [Метрики приложения](#метрики-приложения)
8. [Дашборды](#дашборды)

---

## Обзор архитектуры

```
┌─────────────────────────────────────────────────────────────┐
│                    Grafana Dashboard                         │
│                  (Визуализация и алерты)                    │
└─────────────────────────────────────────────────────────────┘
                    │                       │
         ┌──────────┴──────────┐   ┌────────┴────────┐
         │                     │   │                 │
         ▼                     ▼   ▼                 ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   Prometheus    │   │   Alertmanager  │   │      Loki       │
│   (Метрики)     │   │    (Алерты)     │   │     (Логи)      │
└─────────────────┘   └─────────────────┘   └─────────────────┘
         │                                           │
         │                                  ┌────────┴────────┐
         │                                  │                 │
         ▼                                  ▼                 ▼
┌─────────────────────────────────┐  ┌─────────────────┐
│       Exporters                 │  │    Promtail     │
│ (Node, Postgres, Redis, etc.)   │  │ (Сбор логов)    │
└─────────────────────────────────┘  └─────────────────┘
         │                                  │
         ▼                                  ▼
┌─────────────────────────────────────────────────────────────┐
│                    ServiceDesk Platform                      │
│        (Приложение + PostgreSQL + Redis + Nginx)            │
└─────────────────────────────────────────────────────────────┘
```

---

## Компоненты мониторинга

| Компонент | Порт | Описание |
|-----------|------|----------|
| Prometheus | 9090 | Сбор и хранение метрик |
| Grafana | 3000 | Визуализация и дашборды |
| Alertmanager | 9093 | Обработка и отправка алертов |
| Loki | 3100 | Агрегация логов |
| Promtail | 9080 | Сбор логов из контейнеров |

---

## Настройка Prometheus

### Конфигурационный файл

Файл: `monitoring/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
            - alertmanager:9093

rule_files:
  - /etc/prometheus/alerts.yml

scrape_configs:
  - job_name: 'servicedesk-monolith'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['servicedesk-monolith:8080']
```

### Проверка конфигурации

```bash
# Проверка синтаксиса
docker exec servicedesk-prometheus promtool check config /etc/prometheus/prometheus.yml

# Просмотр целей (targets)
curl http://localhost:9090/api/v1/targets
```

### Основные запросы PromQL

```promql
# Количество запросов в секунду
rate(http_server_requests_seconds_count[5m])

# Время отклика (95-й перцентиль)
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Использование памяти JVM
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# Количество ошибок 5xx
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
```

---

## Настройка Grafana

### Первый вход

1. Откройте http://localhost:3000
2. Логин: `admin`
3. Пароль: значение `GRAFANA_ADMIN_PASSWORD` из `.env`

### Добавление источников данных

Файл: `monitoring/grafana/provisioning/datasources/prometheus.yml`

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
```

### Импорт дашбордов

1. Перейдите в **Dashboards** → **Import**
2. Загрузите JSON файл из `monitoring/grafana/provisioning/dashboards/`
3. Или используйте ID готовых дашбордов:
   - JVM Micrometer: 4701
   - Spring Boot: 10280
   - PostgreSQL: 9628
   - Redis: 763
   - Nginx: 12708

---

## Настройка алертов

### Файл правил алертов

Файл: `monitoring/prometheus/alerts.yml`

```yaml
groups:
  - name: application
    rules:
      - alert: ServiceDown
        expr: up{job="servicedesk-monolith"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "ServiceDesk is down"
          description: "Application has been down for more than 1 minute"

      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
          / sum(rate(http_server_requests_seconds_count[5m])) * 100 > 5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}%"
```

### Конфигурация Alertmanager

Файл: `monitoring/alertmanager/alertmanager.yml`

```yaml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alerts@servicedesk.local'
  smtp_auth_username: 'your-email@gmail.com'
  smtp_auth_password: 'your-app-password'

route:
  receiver: 'default-receiver'
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h

  routes:
    - match:
        severity: critical
      receiver: 'critical-receiver'

receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'admin@servicedesk.local'

  - name: 'critical-receiver'
    email_configs:
      - to: 'oncall@servicedesk.local'
```

### Проверка алертов

```bash
# Просмотр активных алертов
curl http://localhost:9090/api/v1/alerts

# Просмотр правил
curl http://localhost:9090/api/v1/rules
```

---

## Логирование с Loki

### Конфигурация Loki

Файл: `monitoring/loki/loki-config.yml`

```yaml
auth_enabled: false

server:
  http_listen_port: 3100

schema_config:
  configs:
    - from: 2020-10-24
      store: boltdb-shipper
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 24h

limits_config:
  reject_old_samples: true
  reject_old_samples_max_age: 168h  # 7 дней
```

### Конфигурация Promtail

Файл: `monitoring/promtail/promtail-config.yml`

```yaml
server:
  http_listen_port: 9080

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: servicedesk-monolith
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        filters:
          - name: name
            values: ["servicedesk-monolith"]
```

### Запросы LogQL в Grafana

```logql
# Все логи приложения
{container="servicedesk-monolith"}

# Только ошибки
{container="servicedesk-monolith"} |= "ERROR"

# Логи с определённым trace_id
{container="servicedesk-monolith"} | json | trace_id="abc123"

# Количество ошибок по времени
count_over_time({container="servicedesk-monolith"} |= "ERROR" [5m])
```

---

## Метрики приложения

### Включение метрик в Spring Boot

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: servicedesk-monolith
```

### Доступные эндпоинты

| Эндпоинт | Описание |
|----------|----------|
| `/actuator/health` | Состояние приложения |
| `/actuator/info` | Информация о версии |
| `/actuator/metrics` | Список метрик |
| `/actuator/prometheus` | Метрики в формате Prometheus |

### Основные метрики

```
# HTTP запросы
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_bucket

# JVM память
jvm_memory_used_bytes
jvm_memory_max_bytes
jvm_memory_committed_bytes

# JVM потоки
jvm_threads_live_threads
jvm_threads_daemon_threads
jvm_threads_peak_threads

# База данных
hikaricp_connections_active
hikaricp_connections_idle
hikaricp_connections_pending
```

---

## Дашборды

### ServiceDesk Overview Dashboard

Основные панели:
- Статус сервисов (UP/DOWN)
- Количество запросов в секунду
- Время отклика (P50, P95, P99)
- Количество ошибок
- Использование памяти JVM
- Активные подключения к БД

### Импорт дашборда

1. Откройте Grafana
2. Dashboards → Import
3. Загрузите файл: `monitoring/grafana/provisioning/dashboards/servicedesk.json`
4. Выберите источник данных Prometheus

### Создание custom дашборда

1. Создайте новый дашборд
2. Добавьте панели с нужными метриками
3. Настройте пороговые значения
4. Экспортируйте JSON для версионирования

---

## Рекомендации

### 1. Хранение метрик

```yaml
# В docker-compose.prod.yml
prometheus:
  command:
    - '--storage.tsdb.retention.time=15d'
    - '--storage.tsdb.retention.size=10GB'
```

### 2. Безопасность

- Ограничьте доступ к Prometheus и Alertmanager
- Используйте HTTPS для Grafana
- Настройте аутентификацию

### 3. Алерты

- Не создавайте слишком много алертов
- Используйте разные уровни severity
- Настройте правильные thresholds
- Тестируйте алерты регулярно

### 4. Производительность

- Оптимизируйте scrape interval
- Используйте recording rules для сложных запросов
- Мониторьте сам Prometheus

---

## Устранение неполадок

### Prometheus не собирает метрики

```bash
# Проверка целей
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[].health'

# Проверка доступности эндпоинта
curl http://localhost:8080/actuator/prometheus
```

### Grafana не отображает данные

1. Проверьте подключение к Prometheus
2. Проверьте время (синхронизация NTP)
3. Проверьте запрос в Explore

### Alertmanager не отправляет уведомления

```bash
# Проверка конфигурации
docker exec servicedesk-alertmanager amtool check-config /etc/alertmanager/alertmanager.yml

# Просмотр логов
docker logs servicedesk-alertmanager
```

---

## Связанная документация

- [Развёртывание](DEPLOYMENT.md)
- [Бэкап и восстановление](BACKUP-RESTORE.md)
- [Устранение неполадок](TROUBLESHOOTING.md)
