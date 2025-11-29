-- Channels table
CREATE TABLE channels (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    configuration JSONB,
    project_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_channels_project_id ON channels(project_id);
CREATE INDEX idx_channels_type ON channels(type);
CREATE INDEX idx_channels_enabled ON channels(enabled);

-- Email configurations table
CREATE TABLE email_configurations (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,

    -- IMAP settings
    imap_host VARCHAR(255),
    imap_port INTEGER DEFAULT 993,
    imap_username VARCHAR(255),
    imap_password VARCHAR(255),
    imap_ssl BOOLEAN DEFAULT TRUE,
    imap_folder VARCHAR(100) DEFAULT 'INBOX',

    -- SMTP settings
    smtp_host VARCHAR(255),
    smtp_port INTEGER DEFAULT 587,
    smtp_username VARCHAR(255),
    smtp_password VARCHAR(255),
    smtp_ssl BOOLEAN DEFAULT FALSE,
    smtp_tls BOOLEAN DEFAULT TRUE,
    smtp_auth BOOLEAN DEFAULT TRUE,

    -- Display settings
    from_name VARCHAR(255),
    reply_to VARCHAR(255),
    signature TEXT,

    -- Polling settings
    poll_interval_seconds INTEGER DEFAULT 60,
    auto_create_ticket BOOLEAN DEFAULT TRUE,
    default_priority VARCHAR(50) DEFAULT 'MEDIUM',
    default_team_id VARCHAR(36),
    default_category_id VARCHAR(36),

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_poll_at TIMESTAMP,
    last_error TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    CONSTRAINT fk_email_config_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_config_channel_id ON email_configurations(channel_id);
CREATE INDEX idx_email_config_email ON email_configurations(email_address);

-- Email messages table
CREATE TABLE email_messages (
    id VARCHAR(36) PRIMARY KEY,
    message_id VARCHAR(500) UNIQUE,
    channel_id VARCHAR(36) NOT NULL,
    ticket_id VARCHAR(36),

    direction VARCHAR(20) NOT NULL,
    from_address VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    to_addresses TEXT,
    cc_addresses TEXT,
    bcc_addresses TEXT,

    subject VARCHAR(500) NOT NULL,
    body_text TEXT,
    body_html TEXT,

    in_reply_to VARCHAR(500),
    references_header TEXT,

    has_attachments BOOLEAN DEFAULT FALSE,
    attachment_count INTEGER DEFAULT 0,

    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    received_at TIMESTAMP,
    processed_at TIMESTAMP,

    error_message TEXT,
    retry_count INTEGER DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_email_messages_channel_id ON email_messages(channel_id);
CREATE INDEX idx_email_messages_ticket_id ON email_messages(ticket_id);
CREATE INDEX idx_email_messages_status ON email_messages(status);
CREATE INDEX idx_email_messages_direction ON email_messages(direction);
CREATE INDEX idx_email_messages_from ON email_messages(from_address);

-- Telegram configurations table
CREATE TABLE telegram_configurations (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,

    bot_token VARCHAR(255) NOT NULL,
    bot_username VARCHAR(255),

    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(255),
    use_webhook BOOLEAN DEFAULT FALSE,

    welcome_message TEXT,
    auto_create_ticket BOOLEAN DEFAULT TRUE,
    default_priority VARCHAR(50) DEFAULT 'MEDIUM',
    default_team_id VARCHAR(36),
    default_category_id VARCHAR(36),

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_update_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    CONSTRAINT fk_telegram_config_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
);

CREATE INDEX idx_telegram_config_channel_id ON telegram_configurations(channel_id);

-- Telegram messages table
CREATE TABLE telegram_messages (
    id VARCHAR(36) PRIMARY KEY,
    telegram_message_id BIGINT,
    channel_id VARCHAR(36) NOT NULL,
    ticket_id VARCHAR(36),

    chat_id BIGINT NOT NULL,
    chat_type VARCHAR(50),

    from_user_id BIGINT,
    from_username VARCHAR(255),
    from_first_name VARCHAR(255),
    from_last_name VARCHAR(255),

    direction VARCHAR(20) NOT NULL,
    text TEXT,
    message_type VARCHAR(50),

    file_id VARCHAR(255),
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),

    reply_to_message_id BIGINT,

    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_telegram_messages_channel_id ON telegram_messages(channel_id);
CREATE INDEX idx_telegram_messages_ticket_id ON telegram_messages(ticket_id);
CREATE INDEX idx_telegram_messages_chat_id ON telegram_messages(chat_id);
CREATE INDEX idx_telegram_messages_from_user ON telegram_messages(from_user_id);
CREATE UNIQUE INDEX idx_telegram_messages_unique ON telegram_messages(telegram_message_id, chat_id);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_channels_updated_at BEFORE UPDATE ON channels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_email_configurations_updated_at BEFORE UPDATE ON email_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_email_messages_updated_at BEFORE UPDATE ON email_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_telegram_configurations_updated_at BEFORE UPDATE ON telegram_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_telegram_messages_updated_at BEFORE UPDATE ON telegram_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
