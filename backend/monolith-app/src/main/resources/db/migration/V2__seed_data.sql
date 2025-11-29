-- ServiceDesk Platform Seed Data
-- Version 2: Initial Seed Data

-- =============================================================================
-- DEFAULT ADMIN USER (password: admin123)
-- =============================================================================
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, email_verified, locale)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin@servicedesk.local',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- admin123
    'System',
    'Administrator',
    'ADMIN',
    'ACTIVE',
    TRUE,
    'en'
);

-- =============================================================================
-- DEFAULT SUPPORT TEAM
-- =============================================================================
INSERT INTO teams (id, name, description, manager_id)
VALUES (
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Support Team',
    'Default support team',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
);

-- =============================================================================
-- DEMO AGENTS
-- =============================================================================
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, email_verified, locale, team_id)
VALUES
    (
        'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'agent1@servicedesk.local',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- admin123
        'John',
        'Agent',
        'AGENT',
        'ACTIVE',
        TRUE,
        'en',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
    ),
    (
        'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'agent2@servicedesk.local',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- admin123
        'Jane',
        'Support',
        'AGENT',
        'ACTIVE',
        TRUE,
        'ru',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
    );

-- =============================================================================
-- DEMO CUSTOMER
-- =============================================================================
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, email_verified, locale)
VALUES (
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'customer@example.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- admin123
    'Demo',
    'Customer',
    'CUSTOMER',
    'ACTIVE',
    TRUE,
    'en'
);

-- =============================================================================
-- DEFAULT PROJECT
-- =============================================================================
INSERT INTO projects (id, name, key, description, status, lead_id)
VALUES (
    'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'General Support',
    'SUP',
    'General customer support project',
    'ACTIVE',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
);

-- =============================================================================
-- LINK TEAM TO PROJECT
-- =============================================================================
INSERT INTO team_projects (team_id, project_id)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');

-- =============================================================================
-- LINK USERS TO PROJECT
-- =============================================================================
INSERT INTO user_projects (user_id, project_id)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');

-- =============================================================================
-- DEFAULT CATEGORIES
-- =============================================================================
INSERT INTO categories (id, name, description, color, icon, sort_order, project_id, default_team_id)
VALUES
    (
        '10eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Technical Support',
        'Technical issues and troubleshooting',
        '#ef4444',
        'build',
        1,
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
    ),
    (
        '20eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Billing & Payments',
        'Billing, invoicing and payment questions',
        '#f59e0b',
        'payment',
        2,
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        NULL
    ),
    (
        '30eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Feature Requests',
        'New feature suggestions and improvements',
        '#10b981',
        'lightbulb',
        3,
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        NULL
    ),
    (
        '40eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'General Inquiry',
        'General questions and information',
        '#6366f1',
        'help',
        4,
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        NULL
    );

-- =============================================================================
-- DEFAULT SLA POLICIES
-- =============================================================================
INSERT INTO sla_policies (id, name, description, project_id, priority, first_response_hours, resolution_hours, business_hours_only, is_default)
VALUES
    (
        '50eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Urgent SLA',
        'SLA for urgent priority tickets',
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'URGENT',
        1,
        4,
        FALSE,
        FALSE
    ),
    (
        '60eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'High SLA',
        'SLA for high priority tickets',
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'HIGH',
        4,
        24,
        TRUE,
        FALSE
    ),
    (
        '70eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Medium SLA',
        'SLA for medium priority tickets',
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'MEDIUM',
        8,
        48,
        TRUE,
        TRUE
    ),
    (
        '80eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'Low SLA',
        'SLA for low priority tickets',
        'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        'LOW',
        24,
        120,
        TRUE,
        FALSE
    );

-- =============================================================================
-- DEMO TICKET
-- =============================================================================
INSERT INTO tickets (id, ticket_number, subject, description, status, priority, type, channel, project_id, category_id, requester_id, assignee_id, team_id)
VALUES (
    '90eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'SUP-1',
    'Welcome to ServiceDesk Platform',
    'This is a demo ticket to help you get started with the ServiceDesk Platform. Feel free to explore the features!',
    'OPEN',
    'LOW',
    'QUESTION',
    'WEB',
    'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '40eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
);

-- Add tags to demo ticket
INSERT INTO ticket_tags (ticket_id, tag)
VALUES
    ('90eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'demo'),
    ('90eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'welcome');

-- Add history entry
INSERT INTO ticket_history (id, ticket_id, user_id, action, description)
VALUES (
    'a1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '90eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'CREATED',
    'Ticket created'
);

-- Add welcome comment
INSERT INTO ticket_comments (id, ticket_id, author_id, content, type)
VALUES (
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    '90eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Hello! Welcome to ServiceDesk Platform. We are here to help you with any questions or issues. How can we assist you today?',
    'PUBLIC'
);
