-- Problem Management (ITIL) Migration
-- Version 7: Complete Problem Management infrastructure

-- =============================================================================
-- Problems
-- =============================================================================
CREATE TABLE IF NOT EXISTS problems (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    problem_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,

    -- Classification
    category VARCHAR(50),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    impact VARCHAR(20) DEFAULT 'MEDIUM',
    urgency VARCHAR(20) DEFAULT 'MEDIUM',

    -- Status workflow
    status VARCHAR(30) NOT NULL DEFAULT 'IDENTIFIED',
    -- IDENTIFIED -> LOGGED -> DIAGNOSED -> WORKAROUND_AVAILABLE ->
    -- ROOT_CAUSE_IDENTIFIED -> SOLUTION_FOUND -> CLOSED -> VERIFIED

    -- People
    reported_by_id UUID NOT NULL REFERENCES users(id),
    assignee_id UUID REFERENCES users(id),
    team_id UUID REFERENCES teams(id),

    -- Root Cause Analysis
    root_cause TEXT,
    root_cause_category VARCHAR(50), -- HARDWARE, SOFTWARE, PROCESS, PEOPLE, ENVIRONMENT
    rca_completed_at TIMESTAMP,
    rca_completed_by UUID REFERENCES users(id),

    -- Solution
    workaround TEXT,
    workaround_available BOOLEAN DEFAULT FALSE,
    solution TEXT,
    solution_verified BOOLEAN DEFAULT FALSE,
    solution_verified_at TIMESTAMP,
    solution_verified_by UUID REFERENCES users(id),

    -- Metrics
    incident_count INTEGER DEFAULT 0,
    estimated_impact_cost DECIMAL(12, 2),

    -- Relations
    project_id UUID REFERENCES projects(id),
    known_error_id UUID, -- Will be set if converted to known error

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_problem_number ON problems(problem_number);
CREATE INDEX IF NOT EXISTS idx_problem_status ON problems(status);
CREATE INDEX IF NOT EXISTS idx_problem_category ON problems(category);
CREATE INDEX IF NOT EXISTS idx_problem_assignee ON problems(assignee_id);
CREATE INDEX IF NOT EXISTS idx_problem_reporter ON problems(reported_by_id);

-- =============================================================================
-- Known Errors Database (KEDB)
-- =============================================================================
CREATE TABLE IF NOT EXISTS known_errors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    error_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    description TEXT,

    -- Origin
    problem_id UUID REFERENCES problems(id),

    -- Error details
    symptoms TEXT,
    root_cause TEXT,
    workaround TEXT,
    permanent_fix TEXT,

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, RESOLVED, ARCHIVED
    fix_available BOOLEAN DEFAULT FALSE,
    fix_implemented BOOLEAN DEFAULT FALSE,
    fix_implemented_at TIMESTAMP,

    -- Impact
    affected_services TEXT,
    affected_systems TEXT,
    incident_count INTEGER DEFAULT 0,

    -- Classification
    category VARCHAR(50),
    priority VARCHAR(20) DEFAULT 'MEDIUM',

    -- Audit
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_known_error_number ON known_errors(error_number);
CREATE INDEX IF NOT EXISTS idx_known_error_status ON known_errors(status);
CREATE INDEX IF NOT EXISTS idx_known_error_problem ON known_errors(problem_id);

-- =============================================================================
-- Problem-Incident Links
-- =============================================================================
CREATE TABLE IF NOT EXISTS problem_incidents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    link_type VARCHAR(30) DEFAULT 'RELATED', -- CAUSED_BY, SYMPTOM_OF, RELATED
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(problem_id, ticket_id)
);

CREATE INDEX IF NOT EXISTS idx_problem_incident_problem ON problem_incidents(problem_id);
CREATE INDEX IF NOT EXISTS idx_problem_incident_ticket ON problem_incidents(ticket_id);

-- =============================================================================
-- Problem-Asset Links (affected CIs)
-- =============================================================================
CREATE TABLE IF NOT EXISTS problem_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    impact_type VARCHAR(30) DEFAULT 'AFFECTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(problem_id, asset_id)
);

CREATE INDEX IF NOT EXISTS idx_problem_asset_problem ON problem_assets(problem_id);
CREATE INDEX IF NOT EXISTS idx_problem_asset_asset ON problem_assets(asset_id);

-- =============================================================================
-- Root Cause Analysis Templates
-- =============================================================================
CREATE TABLE IF NOT EXISTS rca_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    template_type VARCHAR(30) NOT NULL, -- FIVE_WHYS, FISHBONE, FAULT_TREE, TIMELINE
    questions JSONB, -- List of questions/fields for the template
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- =============================================================================
-- Problem RCA (Root Cause Analysis) Records
-- =============================================================================
CREATE TABLE IF NOT EXISTS problem_rcas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    template_id UUID REFERENCES rca_templates(id),
    analysis_data JSONB, -- Filled-in analysis
    findings TEXT,
    recommendations TEXT,
    conducted_by_id UUID REFERENCES users(id),
    conducted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_problem_rca_problem ON problem_rcas(problem_id);

-- =============================================================================
-- Problem History
-- =============================================================================
CREATE TABLE IF NOT EXISTS problem_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
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

CREATE INDEX IF NOT EXISTS idx_problem_history_problem ON problem_history(problem_id);
CREATE INDEX IF NOT EXISTS idx_problem_history_date ON problem_history(created_at);

-- =============================================================================
-- Insert default RCA templates
-- =============================================================================
INSERT INTO rca_templates (id, name, description, template_type, questions, is_default) VALUES
    (uuid_generate_v4(), '5 Whys Analysis', 'Simple iterative technique to explore cause-and-effect relationships',
     'FIVE_WHYS',
     '{"questions": ["What happened?", "Why did it happen? (1)", "Why? (2)", "Why? (3)", "Why? (4)", "Why? (5 - root cause)"]}',
     true),
    (uuid_generate_v4(), 'Fishbone Diagram', 'Ishikawa diagram for categorizing potential causes',
     'FISHBONE',
     '{"categories": ["People", "Process", "Technology", "Environment", "Materials", "Methods"], "questions": ["What is the problem statement?", "What are the potential causes in each category?"]}',
     false),
    (uuid_generate_v4(), 'Timeline Analysis', 'Chronological analysis of events leading to the problem',
     'TIMELINE',
     '{"questions": ["When was the problem first detected?", "What events occurred before detection?", "What changes were made recently?", "What is the sequence of events?"]}',
     false)
ON CONFLICT DO NOTHING;
