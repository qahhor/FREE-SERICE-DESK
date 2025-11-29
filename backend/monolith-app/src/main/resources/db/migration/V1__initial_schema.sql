-- ServiceDesk Platform Database Schema
-- Version 1: Initial Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- USERS
-- =============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    locale VARCHAR(10) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'UTC',
    last_login_at TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    team_id UUID
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_team ON users(team_id);

-- =============================================================================
-- TEAMS
-- =============================================================================
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    email VARCHAR(255),
    manager_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_teams_name ON teams(name);
CREATE INDEX idx_teams_manager ON teams(manager_id);

-- Add foreign key to users table
ALTER TABLE users ADD CONSTRAINT fk_users_team FOREIGN KEY (team_id) REFERENCES teams(id);

-- =============================================================================
-- PROJECTS
-- =============================================================================
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    key VARCHAR(10) NOT NULL UNIQUE,
    description TEXT,
    logo_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    lead_id UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_projects_key ON projects(key);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_lead ON projects(lead_id);

-- =============================================================================
-- USER_PROJECTS (Many-to-Many)
-- =============================================================================
CREATE TABLE user_projects (
    user_id UUID NOT NULL REFERENCES users(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    PRIMARY KEY (user_id, project_id)
);

-- =============================================================================
-- TEAM_PROJECTS (Many-to-Many)
-- =============================================================================
CREATE TABLE team_projects (
    team_id UUID NOT NULL REFERENCES teams(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    PRIMARY KEY (team_id, project_id)
);

-- =============================================================================
-- CATEGORIES
-- =============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7) DEFAULT '#6366f1',
    icon VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    project_id UUID NOT NULL REFERENCES projects(id),
    parent_id UUID REFERENCES categories(id),
    default_assignee_id UUID REFERENCES users(id),
    default_team_id UUID REFERENCES teams(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_categories_project ON categories(project_id);
CREATE INDEX idx_categories_parent ON categories(parent_id);

-- =============================================================================
-- TICKETS
-- =============================================================================
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_number VARCHAR(20) NOT NULL UNIQUE,
    subject VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    type VARCHAR(20) NOT NULL DEFAULT 'QUESTION',
    channel VARCHAR(20) NOT NULL DEFAULT 'WEB',
    project_id UUID NOT NULL REFERENCES projects(id),
    category_id UUID REFERENCES categories(id),
    requester_id UUID NOT NULL REFERENCES users(id),
    assignee_id UUID REFERENCES users(id),
    team_id UUID REFERENCES teams(id),
    due_date TIMESTAMP,
    first_response_at TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    reopened_count INTEGER DEFAULT 0,
    csat_rating INTEGER,
    csat_comment TEXT,
    external_id VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_ticket_number ON tickets(ticket_number);
CREATE INDEX idx_ticket_status ON tickets(status);
CREATE INDEX idx_ticket_priority ON tickets(priority);
CREATE INDEX idx_ticket_assignee ON tickets(assignee_id);
CREATE INDEX idx_ticket_requester ON tickets(requester_id);
CREATE INDEX idx_ticket_project ON tickets(project_id);
CREATE INDEX idx_ticket_team ON tickets(team_id);
CREATE INDEX idx_ticket_category ON tickets(category_id);
CREATE INDEX idx_ticket_created ON tickets(created_at);
CREATE INDEX idx_ticket_due_date ON tickets(due_date);

-- =============================================================================
-- TICKET_TAGS
-- =============================================================================
CREATE TABLE ticket_tags (
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (ticket_id, tag)
);

CREATE INDEX idx_ticket_tags_tag ON ticket_tags(tag);

-- =============================================================================
-- TICKET_WATCHERS (Many-to-Many)
-- =============================================================================
CREATE TABLE ticket_watchers (
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (ticket_id, user_id)
);

-- =============================================================================
-- TICKET_COMMENTS
-- =============================================================================
CREATE TABLE ticket_comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    content_html TEXT,
    type VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    channel VARCHAR(20),
    external_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_comments_ticket ON ticket_comments(ticket_id);
CREATE INDEX idx_comments_author ON ticket_comments(author_id);
CREATE INDEX idx_comments_created ON ticket_comments(created_at);

-- =============================================================================
-- TICKET_ATTACHMENTS
-- =============================================================================
CREATE TABLE ticket_attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    comment_id UUID REFERENCES ticket_comments(id) ON DELETE SET NULL,
    uploaded_by_id UUID NOT NULL REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    thumbnail_path VARCHAR(500),
    checksum VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_attachments_ticket ON ticket_attachments(ticket_id);
CREATE INDEX idx_attachments_comment ON ticket_attachments(comment_id);

-- =============================================================================
-- TICKET_HISTORY
-- =============================================================================
CREATE TABLE ticket_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    field_name VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_history_ticket ON ticket_history(ticket_id);
CREATE INDEX idx_history_user ON ticket_history(user_id);
CREATE INDEX idx_history_action ON ticket_history(action);
CREATE INDEX idx_history_created ON ticket_history(created_at);

-- =============================================================================
-- SLA_POLICIES
-- =============================================================================
CREATE TABLE sla_policies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    project_id UUID REFERENCES projects(id),
    priority VARCHAR(20),
    first_response_hours INTEGER NOT NULL,
    resolution_hours INTEGER NOT NULL,
    business_hours_only BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_sla_project ON sla_policies(project_id);
CREATE INDEX idx_sla_priority ON sla_policies(priority);

-- =============================================================================
-- AUTOMATION_RULES
-- =============================================================================
CREATE TABLE automation_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    project_id UUID REFERENCES projects(id),
    trigger_type VARCHAR(50) NOT NULL,
    trigger_conditions JSONB NOT NULL,
    actions JSONB NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    execution_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_rules_project ON automation_rules(project_id);
CREATE INDEX idx_rules_trigger ON automation_rules(trigger_type);
CREATE INDEX idx_rules_active ON automation_rules(is_active);

-- =============================================================================
-- CSAT_SURVEYS
-- =============================================================================
CREATE TABLE csat_surveys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_csat_ticket ON csat_surveys(ticket_id);
CREATE INDEX idx_csat_user ON csat_surveys(user_id);
CREATE INDEX idx_csat_rating ON csat_surveys(rating);
