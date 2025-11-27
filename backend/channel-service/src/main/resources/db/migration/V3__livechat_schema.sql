-- Live Chat Sessions table
CREATE TABLE livechat_sessions (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    visitor_id VARCHAR(100) NOT NULL,
    visitor_name VARCHAR(255),
    visitor_email VARCHAR(255),
    visitor_phone VARCHAR(50),

    assigned_agent_id VARCHAR(36),
    assigned_agent_name VARCHAR(255),
    ticket_id VARCHAR(36),

    status VARCHAR(50) NOT NULL DEFAULT 'WAITING',
    department VARCHAR(100),

    page_url VARCHAR(1000),
    page_title VARCHAR(500),
    user_agent TEXT,
    ip_address VARCHAR(50),
    locale VARCHAR(10) DEFAULT 'en',

    queue_position INTEGER,

    started_at TIMESTAMP,
    assigned_at TIMESTAMP,
    ended_at TIMESTAMP,
    last_activity_at TIMESTAMP,

    message_count INTEGER DEFAULT 0,

    rating INTEGER,
    feedback TEXT,

    visitor_info JSONB,
    custom_fields JSONB,
    tags VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_livechat_sessions_project_id ON livechat_sessions(project_id);
CREATE INDEX idx_livechat_sessions_visitor_id ON livechat_sessions(visitor_id);
CREATE INDEX idx_livechat_sessions_status ON livechat_sessions(status);
CREATE INDEX idx_livechat_sessions_assigned_agent ON livechat_sessions(assigned_agent_id);
CREATE INDEX idx_livechat_sessions_department ON livechat_sessions(department);
CREATE INDEX idx_livechat_sessions_created_at ON livechat_sessions(created_at);
CREATE INDEX idx_livechat_sessions_last_activity ON livechat_sessions(last_activity_at);

-- Live Chat Messages table
CREATE TABLE livechat_messages (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,

    sender_type VARCHAR(50) NOT NULL,
    sender_id VARCHAR(36),
    sender_name VARCHAR(255),
    sender_avatar VARCHAR(500),

    content TEXT NOT NULL,
    message_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',

    attachment_url VARCHAR(1000),
    attachment_name VARCHAR(255),
    attachment_size BIGINT,
    attachment_mime_type VARCHAR(100),

    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    delivered_at TIMESTAMP,

    metadata JSONB,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),

    CONSTRAINT fk_livechat_message_session FOREIGN KEY (session_id)
        REFERENCES livechat_sessions(id) ON DELETE CASCADE
);

CREATE INDEX idx_livechat_messages_session_id ON livechat_messages(session_id);
CREATE INDEX idx_livechat_messages_sender_type ON livechat_messages(sender_type);
CREATE INDEX idx_livechat_messages_created_at ON livechat_messages(created_at);
CREATE INDEX idx_livechat_messages_is_read ON livechat_messages(is_read);

-- Agent Availability table (for persistent storage)
CREATE TABLE agent_availability (
    id VARCHAR(36) PRIMARY KEY,
    agent_id VARCHAR(36) NOT NULL UNIQUE,
    agent_name VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),

    status VARCHAR(50) NOT NULL DEFAULT 'OFFLINE',
    max_chats INTEGER DEFAULT 5,
    active_chats INTEGER DEFAULT 0,

    departments TEXT,
    skills TEXT,

    last_active_at TIMESTAMP,
    went_offline_at TIMESTAMP,

    auto_accept BOOLEAN DEFAULT FALSE,
    sound_enabled BOOLEAN DEFAULT TRUE,
    desktop_notifications BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_agent_availability_status ON agent_availability(status);
CREATE INDEX idx_agent_availability_last_active ON agent_availability(last_active_at);

-- Canned Responses table
CREATE TABLE canned_responses (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36),
    agent_id VARCHAR(36),

    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100),
    tags VARCHAR(500),
    shortcut VARCHAR(50),

    is_global BOOLEAN DEFAULT FALSE,
    usage_count INTEGER DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_canned_responses_project_id ON canned_responses(project_id);
CREATE INDEX idx_canned_responses_agent_id ON canned_responses(agent_id);
CREATE INDEX idx_canned_responses_category ON canned_responses(category);
CREATE INDEX idx_canned_responses_shortcut ON canned_responses(shortcut);

-- Chat Routing Rules table
CREATE TABLE chat_routing_rules (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    priority INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,

    conditions JSONB NOT NULL,
    actions JSONB NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36)
);

CREATE INDEX idx_chat_routing_rules_project ON chat_routing_rules(project_id);
CREATE INDEX idx_chat_routing_rules_enabled ON chat_routing_rules(enabled);

-- Triggers
CREATE TRIGGER update_livechat_sessions_updated_at BEFORE UPDATE ON livechat_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_livechat_messages_updated_at BEFORE UPDATE ON livechat_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agent_availability_updated_at BEFORE UPDATE ON agent_availability
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_canned_responses_updated_at BEFORE UPDATE ON canned_responses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chat_routing_rules_updated_at BEFORE UPDATE ON chat_routing_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
