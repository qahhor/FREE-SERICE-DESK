-- Create databases for all services
CREATE DATABASE servicedesk;
CREATE DATABASE servicedesk_marketplace;
CREATE DATABASE servicedesk_analytics;
CREATE DATABASE servicedesk_channel;
CREATE DATABASE servicedesk_knowledge;
CREATE DATABASE servicedesk_notification;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE servicedesk TO servicedesk;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_marketplace TO servicedesk;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_analytics TO servicedesk;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_channel TO servicedesk;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_knowledge TO servicedesk;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_notification TO servicedesk;
