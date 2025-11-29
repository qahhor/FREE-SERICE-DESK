-- Change Management (ITIL) Migration
-- Version 6: Complete Change Management infrastructure

-- =============================================================================
-- Change Requests
-- =============================================================================
CREATE TABLE IF NOT EXISTS change_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    change_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    justification TEXT,

    -- Classification
    change_type VARCHAR(30) NOT NULL DEFAULT 'NORMAL', -- STANDARD, NORMAL, EMERGENCY
    category VARCHAR(50), -- APPLICATION, INFRASTRUCTURE, DATABASE, NETWORK, SECURITY
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    risk_level VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    impact VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL

    -- Status workflow
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    -- DRAFT -> SUBMITTED -> PENDING_APPROVAL -> APPROVED -> SCHEDULED ->
    -- IN_PROGRESS -> COMPLETED / FAILED / ROLLED_BACK -> CLOSED

    -- People
    requester_id UUID NOT NULL REFERENCES users(id),
    assignee_id UUID REFERENCES users(id),
    team_id UUID REFERENCES teams(id),

    -- Scheduling
    scheduled_start TIMESTAMP,
    scheduled_end TIMESTAMP,
    actual_start TIMESTAMP,
    actual_end TIMESTAMP,

    -- Implementation details
    implementation_plan TEXT,
    rollback_plan TEXT,
    test_plan TEXT,
    communication_plan TEXT,

    -- Post-implementation
    review_notes TEXT,
    success BOOLEAN,

    -- Relations
    project_id UUID REFERENCES projects(id),

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_change_number ON change_requests(change_number);
CREATE INDEX IF NOT EXISTS idx_change_status ON change_requests(status);
CREATE INDEX IF NOT EXISTS idx_change_type ON change_requests(change_type);
CREATE INDEX IF NOT EXISTS idx_change_requester ON change_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_change_assignee ON change_requests(assignee_id);
CREATE INDEX IF NOT EXISTS idx_change_scheduled ON change_requests(scheduled_start);

-- =============================================================================
-- Change Approvers
-- =============================================================================
CREATE TABLE IF NOT EXISTS change_approvals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    change_request_id UUID NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    approver_id UUID NOT NULL REFERENCES users(id),
    approval_level INTEGER NOT NULL DEFAULT 1, -- 1 = first level, 2 = CAB, etc.
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, DELEGATED
    decision_date TIMESTAMP,
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_approval_change ON change_approvals(change_request_id);
CREATE INDEX IF NOT EXISTS idx_approval_approver ON change_approvals(approver_id);
CREATE INDEX IF NOT EXISTS idx_approval_status ON change_approvals(status);

-- =============================================================================
-- Change-Ticket Links
-- =============================================================================
CREATE TABLE IF NOT EXISTS change_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    change_request_id UUID NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    link_type VARCHAR(30) DEFAULT 'RELATED', -- CAUSED_BY, IMPLEMENTS, RELATED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(change_request_id, ticket_id)
);

CREATE INDEX IF NOT EXISTS idx_change_ticket_change ON change_tickets(change_request_id);
CREATE INDEX IF NOT EXISTS idx_change_ticket_ticket ON change_tickets(ticket_id);

-- =============================================================================
-- Change-Asset Links (affected CIs)
-- =============================================================================
CREATE TABLE IF NOT EXISTS change_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    change_request_id UUID NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    impact_type VARCHAR(30) DEFAULT 'AFFECTED', -- AFFECTED, MODIFIED, REPLACED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(change_request_id, asset_id)
);

CREATE INDEX IF NOT EXISTS idx_change_asset_change ON change_assets(change_request_id);
CREATE INDEX IF NOT EXISTS idx_change_asset_asset ON change_assets(asset_id);

-- =============================================================================
-- Change History
-- =============================================================================
CREATE TABLE IF NOT EXISTS change_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    change_request_id UUID NOT NULL REFERENCES change_requests(id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS idx_change_history_change ON change_history(change_request_id);
CREATE INDEX IF NOT EXISTS idx_change_history_date ON change_history(created_at);

-- =============================================================================
-- Change Blackout Windows (no-change periods)
-- =============================================================================
CREATE TABLE IF NOT EXISTS change_blackouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    applies_to VARCHAR(50) DEFAULT 'ALL', -- ALL, NORMAL, STANDARD
    project_id UUID REFERENCES projects(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_blackout_dates ON change_blackouts(start_date, end_date);
