-- User Onboarding Progress
CREATE TABLE user_onboarding (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    tenant_id UUID NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    current_step INTEGER DEFAULT 0,
    total_steps INTEGER DEFAULT 0,
    completed_steps JSONB DEFAULT '{}',
    tour_progress JSONB DEFAULT '{}',
    welcome_dismissed BOOLEAN DEFAULT FALSE,
    tour_completed BOOLEAN DEFAULT FALSE,
    checklist_completed BOOLEAN DEFAULT FALSE,
    hints_enabled BOOLEAN DEFAULT TRUE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_onboarding_user ON user_onboarding(user_id);
CREATE INDEX idx_onboarding_tenant ON user_onboarding(tenant_id);
CREATE INDEX idx_onboarding_status ON user_onboarding(status);

-- Onboarding Steps Definition
CREATE TABLE onboarding_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    step_id VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    target_role VARCHAR(50) NOT NULL,
    display_order INTEGER NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    icon VARCHAR(50),
    action_route VARCHAR(255),
    action_label VARCHAR(100),
    video_url TEXT,
    help_url TEXT,
    estimated_minutes INTEGER DEFAULT 2,
    completion_condition JSONB,
    tour_config JSONB
);

CREATE INDEX idx_onboarding_steps_role ON onboarding_steps(target_role);
CREATE INDEX idx_onboarding_steps_order ON onboarding_steps(display_order);
CREATE INDEX idx_onboarding_steps_active ON onboarding_steps(is_active);

-- Seed onboarding steps for AGENT role
INSERT INTO onboarding_steps (step_id, title, description, type, target_role, display_order, icon, action_route, action_label, estimated_minutes, completion_condition, tour_config) VALUES
-- Welcome & Profile
('agent_welcome', 'Welcome to Service Desk', 'Get familiar with the platform interface and main features.', 'TOUR', 'AGENT', 1, 'waving_hand', '/dashboard', 'Start Tour', 3, NULL,
'[
  {"elementSelector": ".sidebar-nav", "title": "Navigation Menu", "content": "Access all main sections from here: Dashboard, Tickets, Knowledge Base, and more.", "position": "right"},
  {"elementSelector": ".header-toolbar", "title": "Quick Actions", "content": "Create new tickets, search, and access notifications from the toolbar.", "position": "bottom"},
  {"elementSelector": ".user-menu", "title": "Your Profile", "content": "Manage your profile, settings, and sign out from here.", "position": "bottom-left"}
]'::jsonb),

('agent_profile', 'Complete Your Profile', 'Add your photo and contact information for better team collaboration.', 'ACTION', 'AGENT', 2, 'account_circle', '/settings/profile', 'Edit Profile', 2, '{"type": "profile_updated"}', NULL),

-- Tickets
('agent_view_tickets', 'Explore Ticket Dashboard', 'Learn how to view and filter tickets assigned to you.', 'TOUR', 'AGENT', 3, 'confirmation_number', '/tickets', 'View Tickets', 3, NULL,
'[
  {"elementSelector": ".ticket-filters", "title": "Filter Tickets", "content": "Filter tickets by status, priority, assignee, and more.", "position": "bottom"},
  {"elementSelector": ".ticket-list", "title": "Ticket List", "content": "Click on any ticket to view details and take action.", "position": "right"},
  {"elementSelector": ".ticket-stats", "title": "Quick Stats", "content": "See your ticket statistics at a glance.", "position": "bottom"}
]'::jsonb),

('agent_create_ticket', 'Create Your First Ticket', 'Practice creating a test ticket to understand the process.', 'ACTION', 'AGENT', 4, 'add_circle', '/tickets/create', 'Create Ticket', 5, '{"type": "ticket_created", "count": 1}', NULL),

('agent_respond_ticket', 'Respond to a Ticket', 'Learn how to add comments and communicate with customers.', 'ACTION', 'AGENT', 5, 'reply', '/tickets', 'Reply to Ticket', 3, '{"type": "comment_added", "count": 1}', NULL),

-- Knowledge Base
('agent_knowledge_base', 'Explore Knowledge Base', 'Find answers quickly using the knowledge base.', 'TOUR', 'AGENT', 6, 'menu_book', '/knowledge', 'Open Knowledge Base', 3, NULL,
'[
  {"elementSelector": ".kb-search", "title": "Search Articles", "content": "Search for solutions by keywords or phrases.", "position": "bottom"},
  {"elementSelector": ".kb-categories", "title": "Categories", "content": "Browse articles by category for organized access.", "position": "right"}
]'::jsonb),

-- Notifications
('agent_notifications', 'Set Up Notifications', 'Configure how you want to receive alerts about ticket updates.', 'ACTION', 'AGENT', 7, 'notifications', '/settings/notifications', 'Configure Notifications', 2, '{"type": "notifications_configured"}', NULL),

-- Keyboard Shortcuts
('agent_shortcuts', 'Learn Keyboard Shortcuts', 'Speed up your work with keyboard shortcuts.', 'LINK', 'AGENT', 8, 'keyboard', '/help/shortcuts', 'View Shortcuts', 2, NULL, NULL);

-- Seed onboarding steps for ADMIN role
INSERT INTO onboarding_steps (step_id, title, description, type, target_role, display_order, icon, action_route, action_label, estimated_minutes, completion_condition, tour_config) VALUES
('admin_welcome', 'Welcome Administrator', 'Overview of admin capabilities and settings.', 'TOUR', 'ADMIN', 1, 'admin_panel_settings', '/admin', 'Start Tour', 5, NULL,
'[
  {"elementSelector": ".admin-sidebar", "title": "Admin Menu", "content": "Access all administrative functions from here.", "position": "right"},
  {"elementSelector": ".admin-dashboard", "title": "Admin Dashboard", "content": "Monitor system health, user activity, and key metrics.", "position": "bottom"}
]'::jsonb),

('admin_users', 'Manage Users', 'Learn how to add, edit, and manage user accounts.', 'TOUR', 'ADMIN', 2, 'people', '/admin/users', 'Manage Users', 5, NULL,
'[
  {"elementSelector": ".user-list", "title": "User List", "content": "View all users in your organization.", "position": "right"},
  {"elementSelector": ".add-user-btn", "title": "Add Users", "content": "Invite new team members to the platform.", "position": "bottom"}
]'::jsonb),

('admin_invite_user', 'Invite First Team Member', 'Add a colleague to start collaborating.', 'ACTION', 'ADMIN', 3, 'person_add', '/admin/users/invite', 'Invite User', 3, '{"type": "user_invited", "count": 1}', NULL),

('admin_teams', 'Set Up Teams', 'Organize your agents into teams for better ticket routing.', 'ACTION', 'ADMIN', 4, 'groups', '/admin/teams', 'Create Team', 5, '{"type": "team_created", "count": 1}', NULL),

('admin_sla', 'Configure SLA Policies', 'Set up response and resolution time targets.', 'ACTION', 'ADMIN', 5, 'timer', '/admin/sla', 'Configure SLA', 10, '{"type": "sla_created", "count": 1}', NULL),

('admin_channels', 'Enable Support Channels', 'Connect email, chat, or other channels for ticket intake.', 'ACTION', 'ADMIN', 6, 'hub', '/admin/channels', 'Setup Channels', 10, '{"type": "channel_configured", "count": 1}', NULL),

('admin_branding', 'Customize Branding', 'Add your logo and customize the platform appearance.', 'ACTION', 'ADMIN', 7, 'palette', '/admin/settings/branding', 'Customize', 5, '{"type": "branding_updated"}', NULL),

('admin_marketplace', 'Explore Marketplace', 'Discover and install modules to extend functionality.', 'LINK', 'ADMIN', 8, 'store', '/marketplace', 'Browse Modules', 5, NULL, NULL);

-- Seed onboarding steps for CUSTOMER role
INSERT INTO onboarding_steps (step_id, title, description, type, target_role, display_order, icon, action_route, action_label, estimated_minutes, completion_condition, tour_config) VALUES
('customer_welcome', 'Welcome to Support Portal', 'Learn how to get help quickly.', 'TOUR', 'CUSTOMER', 1, 'support_agent', '/portal', 'Start Tour', 2, NULL,
'[
  {"elementSelector": ".portal-search", "title": "Search for Help", "content": "Search our knowledge base for instant answers.", "position": "bottom"},
  {"elementSelector": ".submit-ticket-btn", "title": "Submit a Request", "content": "Cannot find an answer? Submit a support request here.", "position": "bottom"}
]'::jsonb),

('customer_profile', 'Complete Your Profile', 'Help us serve you better with your contact information.', 'ACTION', 'CUSTOMER', 2, 'account_circle', '/portal/profile', 'Edit Profile', 2, '{"type": "profile_updated"}', NULL),

('customer_create_ticket', 'Submit Your First Request', 'Create a support ticket to get help from our team.', 'ACTION', 'CUSTOMER', 3, 'add_circle', '/portal/tickets/new', 'Submit Request', 3, '{"type": "ticket_created", "count": 1}', NULL),

('customer_track_ticket', 'Track Your Requests', 'Learn how to check status and respond to updates.', 'TOUR', 'CUSTOMER', 4, 'visibility', '/portal/tickets', 'View Requests', 2, NULL,
'[
  {"elementSelector": ".ticket-status", "title": "Request Status", "content": "Track the progress of your requests here.", "position": "right"},
  {"elementSelector": ".ticket-reply", "title": "Add Information", "content": "Add comments or attachments to help resolve your request faster.", "position": "bottom"}
]'::jsonb);

-- Update trigger
CREATE TRIGGER update_user_onboarding_updated_at
    BEFORE UPDATE ON user_onboarding
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
