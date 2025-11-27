-- Create additional databases for microservices
-- This script runs automatically when PostgreSQL container starts

-- Create database for channel service
CREATE DATABASE servicedesk_channels;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_channels TO servicedesk;

-- Create database for knowledge service
CREATE DATABASE servicedesk_knowledge;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_knowledge TO servicedesk;

-- Create database for notification service
CREATE DATABASE servicedesk_notifications;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_notifications TO servicedesk;

-- Create database for marketplace service
CREATE DATABASE servicedesk_marketplace;
GRANT ALL PRIVILEGES ON DATABASE servicedesk_marketplace TO servicedesk;

-- Grant schema permissions
\c servicedesk_channels
GRANT ALL ON SCHEMA public TO servicedesk;

\c servicedesk_knowledge
GRANT ALL ON SCHEMA public TO servicedesk;

\c servicedesk_notifications
GRANT ALL ON SCHEMA public TO servicedesk;

\c servicedesk_marketplace
GRANT ALL ON SCHEMA public TO servicedesk;

\c servicedesk
GRANT ALL ON SCHEMA public TO servicedesk;
