# Устранение неполадок ServiceDesk Platform

Руководство по диагностике и устранению типичных проблем.

## Содержание

1. [Диагностика](#диагностика)
2. [Проблемы запуска](#проблемы-запуска)
3. [Проблемы с базой данных](#проблемы-с-базой-данных)
4. [Проблемы с сетью](#проблемы-с-сетью)
5. [Проблемы производительности](#проблемы-производительности)
6. [Проблемы с SSL](#проблемы-с-ssl)
7. [Проблемы с мониторингом](#проблемы-с-мониторингом)
8. [Логи и отладка](#логи-и-отладка)

---

## Диагностика

### Быстрая проверка состояния

```bash
# Статус всех контейнеров
docker compose -f docker-compose.prod.yml ps

# Проверка здоровья приложения
curl -s http://localhost:8080/actuator/health | jq

# Проверка использования ресурсов
docker stats --no-stream

# Проверка логов на ошибки
docker compose -f docker-compose.prod.yml logs --tail=100 | grep -i error
```

### Скрипт диагностики

```bash
#!/bin/bash
echo "=== ServiceDesk Diagnostics ==="
echo ""
echo "1. Container Status:"
docker compose -f docker-compose.prod.yml ps
echo ""
echo "2. Health Check:"
curl -s http://localhost:8080/actuator/health | jq -r '.status'
echo ""
echo "3. Disk Usage:"
df -h /
echo ""
echo "4. Memory Usage:"
free -h
echo ""
echo "5. Recent Errors:"
docker compose -f docker-compose.prod.yml logs --tail=20 2>&1 | grep -i -E "(error|exception|fail)"
```

---

## Проблемы запуска

### Контейнер не запускается

**Симптомы:** Контейнер в состоянии "Restarting" или "Exited"

```bash
# Просмотр логов контейнера
docker logs servicedesk-monolith --tail 100

# Проверка причины выхода
docker inspect servicedesk-monolith | jq '.[0].State'
```

**Типичные причины:**

1. **Нехватка памяти**
   ```bash
   # Проверка
   free -h
   dmesg | grep -i "out of memory"
   
   # Решение: увеличьте RAM или уменьшите лимиты в docker-compose
   ```

2. **Порт занят**
   ```bash
   # Проверка
   sudo netstat -tlnp | grep :8080
   
   # Решение: остановите конфликтующий процесс или измените порт
   ```

3. **Ошибка конфигурации**
   ```bash
   # Проверка .env файла
   docker compose -f docker-compose.prod.yml config
   ```

### База данных не готова

**Симптомы:** "Connection refused" в логах приложения

```bash
# Проверка статуса PostgreSQL
docker exec servicedesk-postgres pg_isready

# Ожидание готовности БД
until docker exec servicedesk-postgres pg_isready; do
    echo "Waiting for PostgreSQL..."
    sleep 2
done
```

### Недостаточно места на диске

```bash
# Проверка места
df -h

# Очистка Docker
docker system prune -a --volumes

# Удаление старых логов
find /var/lib/docker/containers -name "*.log" -size +100M -exec truncate -s 0 {} \;
```

---

## Проблемы с базой данных

### Не удаётся подключиться к PostgreSQL

```bash
# Проверка контейнера
docker exec servicedesk-postgres pg_isready -U servicedesk -d servicedesk

# Проверка логов PostgreSQL
docker logs servicedesk-postgres --tail 50

# Проверка подключения изнутри контейнера приложения
docker exec servicedesk-monolith sh -c 'pg_isready -h postgres -U servicedesk'
```

**Решения:**

1. Проверьте переменные окружения DB_HOST, DB_USER, DB_PASSWORD
2. Убедитесь, что PostgreSQL запустился (`service_healthy`)
3. Проверьте сетевое подключение между контейнерами

### Медленные запросы

```bash
# Включение логирования медленных запросов
docker exec servicedesk-postgres psql -U servicedesk -c "ALTER SYSTEM SET log_min_duration_statement = '1000';"
docker exec servicedesk-postgres psql -U servicedesk -c "SELECT pg_reload_conf();"

# Просмотр активных запросов
docker exec servicedesk-postgres psql -U servicedesk -c "SELECT pid, now() - pg_stat_activity.query_start AS duration, query FROM pg_stat_activity WHERE state != 'idle' ORDER BY duration DESC;"
```

### Переполнение пула соединений

**Симптомы:** "Cannot acquire connection from pool"

```bash
# Проверка количества соединений
docker exec servicedesk-postgres psql -U servicedesk -c "SELECT count(*) FROM pg_stat_activity;"

# Увеличение максимального количества соединений
# В .env: DB_POOL_SIZE=30
```

### Повреждение базы данных

```bash
# Проверка целостности
docker exec servicedesk-postgres psql -U servicedesk -c "SELECT datname, age(datfrozenxid) FROM pg_database;"

# VACUUM для освобождения места
docker exec servicedesk-postgres psql -U servicedesk -c "VACUUM FULL ANALYZE;"
```

---

## Проблемы с сетью

### Контейнеры не видят друг друга

```bash
# Проверка сети
docker network ls
docker network inspect servicedesk_servicedesk-network

# Проверка DNS разрешения
docker exec servicedesk-monolith nslookup postgres
docker exec servicedesk-monolith ping -c 3 postgres
```

### Nginx не проксирует запросы

```bash
# Проверка конфигурации Nginx
docker exec servicedesk-nginx nginx -t

# Проверка доступности upstream
docker exec servicedesk-nginx curl -s http://servicedesk-monolith:8080/actuator/health

# Просмотр логов Nginx
docker logs servicedesk-nginx --tail 50
```

### Порты не доступны извне

```bash
# Проверка привязки портов
docker port servicedesk-nginx

# Проверка файрвола
sudo ufw status
sudo iptables -L -n

# Проверка из внешней сети
curl -v https://servicedesk.yourdomain.com
```

---

## Проблемы производительности

### Высокое использование CPU

```bash
# Определение проблемного контейнера
docker stats --no-stream

# Профилирование Java приложения
docker exec servicedesk-monolith jcmd 1 VM.flags
docker exec servicedesk-monolith jcmd 1 Thread.print

# Проверка GC
docker exec servicedesk-monolith jstat -gc 1
```

**Решения:**
- Увеличьте CPU limits в docker-compose
- Оптимизируйте запросы к БД
- Включите кэширование Redis

### Высокое использование памяти

```bash
# Анализ heap памяти
docker exec servicedesk-monolith jcmd 1 GC.heap_info

# Создание heap dump
docker exec servicedesk-monolith jcmd 1 GC.heap_dump /tmp/heapdump.hprof
docker cp servicedesk-monolith:/tmp/heapdump.hprof ./heapdump.hprof

# Принудительный GC
docker exec servicedesk-monolith jcmd 1 GC.run
```

### Медленные ответы

```bash
# Проверка времени ответа
curl -w "@curl-format.txt" -o /dev/null -s https://servicedesk.yourdomain.com/actuator/health

# curl-format.txt:
#     time_namelookup:  %{time_namelookup}s\n
#        time_connect:  %{time_connect}s\n
#     time_appconnect:  %{time_appconnect}s\n
#    time_pretransfer:  %{time_pretransfer}s\n
#       time_redirect:  %{time_redirect}s\n
#  time_starttransfer:  %{time_starttransfer}s\n
#                     ----------\n
#          time_total:  %{time_total}s\n
```

---

## Проблемы с SSL

### Сертификат не найден

```bash
# Проверка наличия файлов
ls -la nginx/ssl/live/servicedesk/

# Проверка симлинка
readlink -f nginx/ssl/live/servicedesk
```

### Сертификат истёк

```bash
# Проверка срока действия
openssl x509 -in nginx/ssl/live/servicedesk/fullchain.pem -noout -dates

# Обновление сертификата
./scripts/renew-ssl.sh
```

### Ошибка SSL handshake

```bash
# Проверка SSL конфигурации
openssl s_client -connect localhost:443 -servername servicedesk.yourdomain.com

# Проверка поддерживаемых протоколов
nmap --script ssl-enum-ciphers -p 443 localhost
```

---

## Проблемы с мониторингом

### Prometheus не собирает метрики

```bash
# Проверка целей
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health: .health}'

# Проверка доступности эндпоинта метрик
curl -s http://localhost:8080/actuator/prometheus | head -20
```

### Grafana не отображает данные

1. Проверьте подключение к Prometheus
2. Проверьте временной диапазон
3. Проверьте синхронизацию времени (NTP)

```bash
# Проверка времени
timedatectl status

# Синхронизация времени
sudo timedatectl set-ntp true
```

### Alertmanager не отправляет уведомления

```bash
# Проверка конфигурации
docker exec servicedesk-alertmanager amtool check-config /etc/alertmanager/alertmanager.yml

# Просмотр активных алертов
curl -s http://localhost:9093/api/v1/alerts | jq

# Тестовая отправка
curl -H "Content-Type: application/json" -d '[{"labels":{"alertname":"TestAlert"}}]' http://localhost:9093/api/v1/alerts
```

---

## Логи и отладка

### Просмотр логов

```bash
# Все логи
docker compose -f docker-compose.prod.yml logs -f

# Логи конкретного сервиса
docker logs servicedesk-monolith -f --tail 100

# Фильтрация по уровню
docker logs servicedesk-monolith 2>&1 | grep -E "(ERROR|WARN)"

# Логи за период
docker logs servicedesk-monolith --since="2024-01-15T00:00:00" --until="2024-01-15T12:00:00"
```

### Включение DEBUG логирования

```bash
# Временно (до перезапуска)
docker exec servicedesk-monolith curl -X POST http://localhost:8080/actuator/loggers/uz.greenwhite.servicedesk -H 'Content-Type: application/json' -d '{"configuredLevel": "DEBUG"}'

# Постоянно (в .env)
LOGGING_LEVEL_ROOT=DEBUG
```

### Анализ логов с Loki

```logql
# Все ошибки за последний час
{container="servicedesk-monolith"} |= "ERROR" | json

# Запросы с временем > 1 секунды
{container="servicedesk-monolith"} | json | request_time > 1000

# Группировка ошибок
sum by (level) (count_over_time({container="servicedesk-monolith"}[1h]))
```

### Трассировка запросов

```bash
# Включение distributed tracing
# Добавьте в .env:
MANAGEMENT_TRACING_ENABLED=true
MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans
```

---

## Полезные команды

### Быстрое исправление

```bash
# Перезапуск проблемного сервиса
docker compose -f docker-compose.prod.yml restart servicedesk-monolith

# Полная перезагрузка
docker compose -f docker-compose.prod.yml down && docker compose -f docker-compose.prod.yml up -d

# Пересоздание контейнера
docker compose -f docker-compose.prod.yml up -d --force-recreate servicedesk-monolith
```

### Очистка

```bash
# Удаление остановленных контейнеров
docker container prune

# Очистка неиспользуемых образов
docker image prune -a

# Полная очистка Docker
docker system prune -a --volumes
```

### Резервное копирование перед изменениями

```bash
# Бэкап БД
./scripts/backup-postgres.sh

# Бэкап конфигураций
tar -czvf config-backup.tar.gz .env nginx/ monitoring/
```

---

## Связанная документация

- [Развёртывание](DEPLOYMENT.md)
- [Мониторинг](MONITORING.md)
- [Бэкап и восстановление](BACKUP-RESTORE.md)
- [Настройка SSL](SSL-SETUP.md)
