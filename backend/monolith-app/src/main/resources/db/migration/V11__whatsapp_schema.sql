-- WhatsApp configurations table
CREATE TABLE whatsapp_configurations (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,

    phone_number_id VARCHAR(100) NOT NULL UNIQUE,
    business_account_id VARCHAR(100) NOT NULL,
    access_token VARCHAR(500) NOT NULL,
    verify_token VARCHAR(255) NOT NULL,

    webhook_url VARCHAR(500),
    app_secret VARCHAR(255),
    display_phone_number VARCHAR(50),

    welcome_message TEXT,
    auto_create_ticket BOOLEAN NOT NULL DEFAULT TRUE,
    default_priority VARCHAR(50) DEFAULT 'MEDIUM',
    default_team_id VARCHAR(36),
    default_category_id VARCHAR(36),

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_error TEXT,
    api_version VARCHAR(20) DEFAULT 'V18_0',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    CONSTRAINT fk_whatsapp_config_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE
);

CREATE INDEX idx_whatsapp_config_channel_id ON whatsapp_configurations(channel_id);
CREATE INDEX idx_whatsapp_config_phone_number_id ON whatsapp_configurations(phone_number_id);

-- WhatsApp messages table
CREATE TABLE whatsapp_messages (
    id VARCHAR(36) PRIMARY KEY,
    whatsapp_message_id VARCHAR(255) UNIQUE,
    channel_id VARCHAR(36) NOT NULL,
    ticket_id VARCHAR(36),
    conversation_id VARCHAR(36),

    from_number VARCHAR(50) NOT NULL,
    to_number VARCHAR(50) NOT NULL,
    contact_name VARCHAR(255),
    profile_name VARCHAR(255),

    direction VARCHAR(20) NOT NULL,
    message_type VARCHAR(50) NOT NULL,

    text TEXT,
    caption TEXT,

    media_id VARCHAR(255),
    media_url VARCHAR(1000),
    media_mime_type VARCHAR(100),
    media_sha256 VARCHAR(255),
    media_filename VARCHAR(255),

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_name VARCHAR(255),
    location_address TEXT,

    contact_vcard TEXT,

    button_text VARCHAR(255),
    button_payload VARCHAR(500),

    template_name VARCHAR(255),
    template_language VARCHAR(20),

    context_message_id VARCHAR(255),
    context_from VARCHAR(50),

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    failed_at TIMESTAMP,

    error_code VARCHAR(50),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_whatsapp_messages_channel_id ON whatsapp_messages(channel_id);
CREATE INDEX idx_whatsapp_messages_ticket_id ON whatsapp_messages(ticket_id);
CREATE INDEX idx_whatsapp_messages_from_number ON whatsapp_messages(from_number);
CREATE INDEX idx_whatsapp_messages_to_number ON whatsapp_messages(to_number);
CREATE INDEX idx_whatsapp_messages_status ON whatsapp_messages(status);
CREATE INDEX idx_whatsapp_messages_direction ON whatsapp_messages(direction);
CREATE INDEX idx_whatsapp_messages_created_at ON whatsapp_messages(created_at);

-- WhatsApp contacts table
CREATE TABLE whatsapp_contacts (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    wa_id VARCHAR(50) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    profile_name VARCHAR(255),
    customer_id VARCHAR(36),
    ticket_id VARCHAR(36),

    first_message_at TIMESTAMP,
    last_message_at TIMESTAMP,
    message_count INTEGER DEFAULT 0,

    is_blocked BOOLEAN DEFAULT FALSE,
    blocked_reason VARCHAR(255),

    metadata JSONB,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    UNIQUE(channel_id, wa_id)
);

CREATE INDEX idx_whatsapp_contacts_channel_id ON whatsapp_contacts(channel_id);
CREATE INDEX idx_whatsapp_contacts_phone_number ON whatsapp_contacts(phone_number);
CREATE INDEX idx_whatsapp_contacts_customer_id ON whatsapp_contacts(customer_id);
CREATE INDEX idx_whatsapp_contacts_last_message ON whatsapp_contacts(last_message_at);

-- Widget conversations table (for Live Chat)
CREATE TABLE IF NOT EXISTS widget_conversations (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    visitor_id VARCHAR(100) NOT NULL,
    visitor_name VARCHAR(255),
    visitor_email VARCHAR(255),
    visitor_phone VARCHAR(50),

    ticket_id VARCHAR(36),
    assigned_agent_id VARCHAR(36),

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    page_url VARCHAR(1000),
    page_title VARCHAR(500),
    user_agent TEXT,
    ip_address VARCHAR(50),
    locale VARCHAR(10) DEFAULT 'en',

    last_message_at TIMESTAMP,
    closed_at TIMESTAMP,

    rating INTEGER,
    feedback TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_widget_conversations_project_id ON widget_conversations(project_id);
CREATE INDEX idx_widget_conversations_visitor_id ON widget_conversations(visitor_id);
CREATE INDEX idx_widget_conversations_status ON widget_conversations(status);
CREATE INDEX idx_widget_conversations_assigned_agent ON widget_conversations(assigned_agent_id);

-- Widget messages table (for Live Chat)
CREATE TABLE IF NOT EXISTS widget_messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,

    sender_type VARCHAR(50) NOT NULL,
    sender_id VARCHAR(36),
    sender_name VARCHAR(255),

    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',

    attachment_url VARCHAR(1000),
    attachment_name VARCHAR(255),
    attachment_size BIGINT,
    attachment_mime_type VARCHAR(100),

    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    CONSTRAINT fk_widget_message_conversation FOREIGN KEY (conversation_id)
        REFERENCES widget_conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_widget_messages_conversation_id ON widget_messages(conversation_id);
CREATE INDEX idx_widget_messages_sender_type ON widget_messages(sender_type);
CREATE INDEX idx_widget_messages_created_at ON widget_messages(created_at);

-- Triggers for updated_at
CREATE TRIGGER update_whatsapp_configurations_updated_at BEFORE UPDATE ON whatsapp_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_whatsapp_messages_updated_at BEFORE UPDATE ON whatsapp_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_whatsapp_contacts_updated_at BEFORE UPDATE ON whatsapp_contacts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_widget_conversations_updated_at BEFORE UPDATE ON widget_conversations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_widget_messages_updated_at BEFORE UPDATE ON widget_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
