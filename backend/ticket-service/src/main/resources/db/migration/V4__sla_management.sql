-- SLA Management Enhancement Migration
-- Version 4: Complete SLA infrastructure

-- =============================================================================
-- Add SLA fields to tickets table
-- =============================================================================
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS first_response_due TIMESTAMP;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS resolution_due TIMESTAMP;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS next_response_due TIMESTAMP;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS sla_breached BOOLEAN DEFAULT FALSE;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS sla_policy_id UUID;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS first_response_breached BOOLEAN DEFAULT FALSE;
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS resolution_breached BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_ticket_first_response_due ON tickets(first_response_due);
CREATE INDEX IF NOT EXISTS idx_ticket_resolution_due ON tickets(resolution_due);
CREATE INDEX IF NOT EXISTS idx_ticket_sla_breached ON tickets(sla_breached);
CREATE INDEX IF NOT EXISTS idx_ticket_sla_policy ON tickets(sla_policy_id);

-- =============================================================================
-- Update SLA Policies table with additional fields
-- =============================================================================
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS priority_order INTEGER DEFAULT 0;
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS business_hours_start VARCHAR(5) DEFAULT '09:00';
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS business_hours_end VARCHAR(5) DEFAULT '18:00';
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS business_days VARCHAR(20) DEFAULT '1,2,3,4,5';
ALTER TABLE sla_policies ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) DEFAULT 'UTC';

-- =============================================================================
-- SLA Targets table
-- =============================================================================
CREATE TABLE IF NOT EXISTS sla_targets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    policy_id UUID NOT NULL REFERENCES sla_policies(id) ON DELETE CASCADE,
    target_type VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    target_minutes INTEGER NOT NULL,
    warning_minutes INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sla_targets_policy ON sla_targets(policy_id);
CREATE INDEX IF NOT EXISTS idx_sla_targets_type ON sla_targets(target_type);
CREATE INDEX IF NOT EXISTS idx_sla_targets_priority ON sla_targets(priority);

-- =============================================================================
-- SLA Conditions table
-- =============================================================================
CREATE TABLE IF NOT EXISTS sla_conditions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    policy_id UUID NOT NULL REFERENCES sla_policies(id) ON DELETE CASCADE,
    field VARCHAR(50) NOT NULL,
    operator VARCHAR(30) NOT NULL,
    value VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sla_conditions_policy ON sla_conditions(policy_id);

-- =============================================================================
-- Escalation Rules table
-- =============================================================================
CREATE TABLE IF NOT EXISTS escalation_rules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sla_policy_id UUID REFERENCES sla_policies(id) ON DELETE SET NULL,
    trigger_type VARCHAR(30) NOT NULL, -- SLA_WARNING, SLA_BREACH, TIME_BASED
    trigger_minutes INTEGER, -- Minutes before/after SLA due
    escalation_level INTEGER NOT NULL DEFAULT 1,
    notify_assignee BOOLEAN DEFAULT TRUE,
    notify_team_lead BOOLEAN DEFAULT FALSE,
    notify_manager BOOLEAN DEFAULT FALSE,
    notify_custom_users TEXT, -- Comma-separated user IDs
    notify_email TEXT, -- External email addresses
    reassign_to_user_id UUID REFERENCES users(id),
    reassign_to_team_id UUID REFERENCES teams(id),
    change_priority VARCHAR(20),
    add_tags TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    execution_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_escalation_rules_policy ON escalation_rules(sla_policy_id);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_trigger ON escalation_rules(trigger_type);
CREATE INDEX IF NOT EXISTS idx_escalation_rules_active ON escalation_rules(is_active);

-- =============================================================================
-- SLA Breach History table (for audit trail)
-- =============================================================================
CREATE TABLE IF NOT EXISTS sla_breach_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    sla_policy_id UUID REFERENCES sla_policies(id) ON DELETE SET NULL,
    breach_type VARCHAR(30) NOT NULL, -- FIRST_RESPONSE, RESOLUTION, NEXT_RESPONSE
    due_at TIMESTAMP NOT NULL,
    breached_at TIMESTAMP NOT NULL,
    breach_duration_minutes INTEGER NOT NULL,
    escalation_triggered BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sla_breach_ticket ON sla_breach_history(ticket_id);
CREATE INDEX IF NOT EXISTS idx_sla_breach_policy ON sla_breach_history(sla_policy_id);
CREATE INDEX IF NOT EXISTS idx_sla_breach_type ON sla_breach_history(breach_type);
CREATE INDEX IF NOT EXISTS idx_sla_breach_date ON sla_breach_history(breached_at);

-- =============================================================================
-- Business Holidays table (for SLA calculations)
-- =============================================================================
CREATE TABLE IF NOT EXISTS business_holidays (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    holiday_date DATE NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_holidays_date ON business_holidays(holiday_date);
CREATE INDEX IF NOT EXISTS idx_holidays_project ON business_holidays(project_id);

-- =============================================================================
-- Add foreign key from tickets to sla_policies
-- =============================================================================
ALTER TABLE tickets ADD CONSTRAINT fk_ticket_sla_policy
    FOREIGN KEY (sla_policy_id) REFERENCES sla_policies(id) ON DELETE SET NULL;

-- =============================================================================
-- Insert default SLA policies
-- =============================================================================
INSERT INTO sla_policies (id, name, description, is_default, enabled, business_hours_only,
    business_hours_start, business_hours_end, business_days, timezone, first_response_hours, resolution_hours)
VALUES
    (uuid_generate_v4(), 'Default SLA Policy', 'Standard SLA for all tickets', true, true, true,
     '09:00', '18:00', '1,2,3,4,5', 'UTC', 4, 24)
ON CONFLICT DO NOTHING;
