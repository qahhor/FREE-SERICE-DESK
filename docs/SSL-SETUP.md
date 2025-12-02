# Настройка SSL сертификатов

Руководство по настройке SSL/TLS сертификатов для ServiceDesk Platform.

## Содержание

1. [Обзор](#обзор)
2. [Let's Encrypt (рекомендуется)](#lets-encrypt-рекомендуется)
3. [Собственные сертификаты](#собственные-сертификаты)
4. [Самоподписанные сертификаты](#самоподписанные-сертификаты)
5. [Обновление сертификатов](#обновление-сертификатов)
6. [Устранение неполадок](#устранение-неполадок)

---

## Обзор

### Архитектура SSL

```
┌─────────────────┐     HTTPS (443)     ┌─────────────────┐
│    Клиент       │ ────────────────────▶│     Nginx       │
│   (Браузер)     │                      │  (SSL терминация)│
└─────────────────┘                      └────────┬────────┘
                                                  │
                                            HTTP (8080)
                                                  │
                                         ┌────────▼────────┐
                                         │   Application   │
                                         │   (Spring Boot) │
                                         └─────────────────┘
```

### Структура файлов

```
nginx/
├── nginx.conf
├── conf.d/
│   └── default.conf
└── ssl/
    ├── dhparam.pem          # DH параметры
    └── live/
        └── servicedesk/     # Symlink на актуальные сертификаты
            ├── fullchain.pem  # Сертификат + промежуточные
            └── privkey.pem    # Приватный ключ
```

---

## Let's Encrypt (рекомендуется)

### Предварительные требования

1. Доменное имя, направленное на ваш сервер
2. Открытый порт 80 для ACME challenge
3. Email для уведомлений от Let's Encrypt

### Автоматическая настройка

```bash
# Установка переменных
export DOMAIN=servicedesk.yourdomain.com
export EMAIL=admin@yourdomain.com

# Запуск скрипта инициализации
chmod +x scripts/init-letsencrypt.sh
./scripts/init-letsencrypt.sh
```

### Ручная настройка

#### 1. Генерация DH параметров

```bash
# Создание директорий
mkdir -p nginx/ssl/live/servicedesk

# Генерация DH параметров (занимает 2-5 минут)
openssl dhparam -out nginx/ssl/dhparam.pem 2048
```

#### 2. Создание временного самоподписанного сертификата

```bash
# Для начального запуска Nginx
openssl req -x509 -nodes -newkey rsa:4096 -days 1 \
    -keyout nginx/ssl/live/servicedesk/privkey.pem \
    -out nginx/ssl/live/servicedesk/fullchain.pem \
    -subj "/CN=servicedesk.yourdomain.com"
```

#### 3. Запуск Nginx

```bash
docker compose -f docker-compose.prod.yml up -d nginx
```

#### 4. Получение сертификата Let's Encrypt

```bash
docker run --rm -it \
    -v $(pwd)/nginx/ssl:/etc/letsencrypt \
    -v $(pwd)/certbot/www:/var/www/certbot \
    certbot/certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email admin@yourdomain.com \
    --agree-tos \
    --no-eff-email \
    -d servicedesk.yourdomain.com
```

#### 5. Перезагрузка Nginx

```bash
docker exec servicedesk-nginx nginx -s reload
```

### Использование staging среды

Для тестирования используйте staging среду Let's Encrypt:

```bash
# Добавьте флаг --staging
STAGING=1 ./scripts/init-letsencrypt.sh
```

---

## Собственные сертификаты

### Если у вас есть сертификаты от CA

```bash
# Создание директории
mkdir -p nginx/ssl/live/servicedesk

# Копирование файлов
cp /path/to/your-domain.crt nginx/ssl/live/servicedesk/fullchain.pem
cp /path/to/your-domain.key nginx/ssl/live/servicedesk/privkey.pem

# Если есть промежуточный сертификат, объедините:
cat /path/to/your-domain.crt /path/to/intermediate.crt > nginx/ssl/live/servicedesk/fullchain.pem

# Генерация DH параметров
openssl dhparam -out nginx/ssl/dhparam.pem 2048

# Установка прав
chmod 600 nginx/ssl/live/servicedesk/privkey.pem
```

### Проверка сертификата

```bash
# Проверка срока действия
openssl x509 -in nginx/ssl/live/servicedesk/fullchain.pem -noout -dates

# Проверка соответствия ключа и сертификата
openssl x509 -in nginx/ssl/live/servicedesk/fullchain.pem -noout -modulus | md5sum
openssl rsa -in nginx/ssl/live/servicedesk/privkey.pem -noout -modulus | md5sum
# Хеши должны совпадать
```

---

## Самоподписанные сертификаты

### Для разработки и тестирования

```bash
# Создание директории
mkdir -p nginx/ssl/live/servicedesk

# Генерация приватного ключа и сертификата
openssl req -x509 -nodes -days 365 -newkey rsa:4096 \
    -keyout nginx/ssl/live/servicedesk/privkey.pem \
    -out nginx/ssl/live/servicedesk/fullchain.pem \
    -subj "/C=RU/ST=Moscow/L=Moscow/O=ServiceDesk/CN=localhost" \
    -addext "subjectAltName=DNS:localhost,DNS:servicedesk.local,IP:127.0.0.1"

# Генерация DH параметров
openssl dhparam -out nginx/ssl/dhparam.pem 2048
```

### Добавление в доверенные (macOS)

```bash
sudo security add-trusted-cert -d -r trustRoot \
    -k /Library/Keychains/System.keychain \
    nginx/ssl/live/servicedesk/fullchain.pem
```

### Добавление в доверенные (Ubuntu)

```bash
sudo cp nginx/ssl/live/servicedesk/fullchain.pem /usr/local/share/ca-certificates/servicedesk.crt
sudo update-ca-certificates
```

---

## Обновление сертификатов

### Автоматическое обновление Let's Encrypt

```bash
# Добавление в cron (ежедневно в полночь)
(crontab -l 2>/dev/null; echo "0 0 * * * /opt/servicedesk/scripts/renew-ssl.sh >> /var/log/certbot-renew.log 2>&1") | crontab -

# Ручной запуск обновления
./scripts/renew-ssl.sh
```

### Скрипт обновления

```bash
#!/bin/bash
# scripts/renew-ssl.sh

docker run --rm \
    -v $(pwd)/nginx/ssl:/etc/letsencrypt \
    -v $(pwd)/certbot/www:/var/www/certbot \
    certbot/certbot renew --quiet

# Перезагрузка Nginx для применения новых сертификатов
docker exec servicedesk-nginx nginx -s reload
```

### Проверка срока действия

```bash
# Проверка срока действия
openssl x509 -in nginx/ssl/live/servicedesk/fullchain.pem -noout -enddate

# Проверка через curl
echo | openssl s_client -servername servicedesk.yourdomain.com \
    -connect servicedesk.yourdomain.com:443 2>/dev/null | \
    openssl x509 -noout -dates
```

---

## Настройка Nginx

### Рекомендуемая конфигурация SSL

```nginx
# nginx/conf.d/default.conf

server {
    listen 443 ssl http2;
    server_name servicedesk.yourdomain.com;

    # SSL сертификаты
    ssl_certificate /etc/nginx/ssl/live/servicedesk/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/live/servicedesk/privkey.pem;
    ssl_dhparam /etc/nginx/ssl/dhparam.pem;

    # Протоколы и шифры
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers off;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;

    # Сессии
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;
    ssl_session_tickets off;

    # OCSP Stapling
    ssl_stapling on;
    ssl_stapling_verify on;
    resolver 8.8.8.8 8.8.4.4 valid=300s;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;

    # ...
}
```

### Перенаправление HTTP на HTTPS

```nginx
server {
    listen 80;
    server_name servicedesk.yourdomain.com;

    # Let's Encrypt challenge
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # Redirect to HTTPS
    location / {
        return 301 https://$server_name$request_uri;
    }
}
```

---

## Устранение неполадок

### Ошибка: сертификат не найден

```bash
# Проверка наличия файлов
ls -la nginx/ssl/live/servicedesk/

# Проверка прав доступа
stat nginx/ssl/live/servicedesk/privkey.pem
```

### Ошибка: несоответствие ключа и сертификата

```bash
# Сравнение modulus
openssl x509 -in fullchain.pem -noout -modulus | openssl md5
openssl rsa -in privkey.pem -noout -modulus | openssl md5

# Хеши должны совпадать
```

### Ошибка: Let's Encrypt rate limit

- Используйте staging среду для тестирования
- Подождите до сброса лимита (обычно 1 час)
- Проверьте логи: `docker logs certbot`

### Ошибка: порт 80 недоступен

```bash
# Проверка процесса на порту 80
sudo netstat -tlnp | grep :80
sudo lsof -i :80

# Остановка conflicting сервиса
sudo systemctl stop apache2  # например
```

### Проверка SSL конфигурации

```bash
# Онлайн проверка
# https://www.ssllabs.com/ssltest/

# Локальная проверка
openssl s_client -connect servicedesk.yourdomain.com:443 -servername servicedesk.yourdomain.com

# Проверка протоколов
nmap --script ssl-enum-ciphers -p 443 servicedesk.yourdomain.com
```

---

## Лучшие практики

### 1. Безопасность ключей

- Храните приватные ключи с правами 600
- Не добавляйте ключи в git
- Используйте отдельные ключи для разных сред

### 2. Мониторинг

- Настройте алерты на срок действия сертификата
- Мониторьте ошибки SSL в логах Nginx
- Регулярно проверяйте SSL рейтинг

### 3. Резервное копирование

```bash
# Бэкап SSL файлов
tar -czvf ssl-backup.tar.gz nginx/ssl/
```

### 4. Документирование

- Записывайте даты обновления сертификатов
- Документируйте процедуры обновления
- Храните контакты CA

---

## Связанная документация

- [Развёртывание](DEPLOYMENT.md)
- [Мониторинг](MONITORING.md)
- [Устранение неполадок](TROUBLESHOOTING.md)
