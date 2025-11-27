-- Notification Service Schema
-- V1: Initial schema for notification service

-- Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    action_url VARCHAR(500),
    entity_type VARCHAR(100),
    entity_id VARCHAR(100),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);
CREATE INDEX idx_notifications_entity ON notifications(entity_type, entity_id);
CREATE INDEX idx_notifications_priority ON notifications(priority);

-- Notification Preferences
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL UNIQUE,

    -- Email notifications
    email_enabled BOOLEAN DEFAULT TRUE,
    email_ticket_assigned BOOLEAN DEFAULT TRUE,
    email_ticket_updated BOOLEAN DEFAULT TRUE,
    email_ticket_commented BOOLEAN DEFAULT TRUE,
    email_ticket_resolved BOOLEAN DEFAULT TRUE,
    email_mentions BOOLEAN DEFAULT TRUE,
    email_sla_alerts BOOLEAN DEFAULT TRUE,
    email_daily_digest BOOLEAN DEFAULT FALSE,
    email_weekly_report BOOLEAN DEFAULT TRUE,

    -- In-app notifications
    inapp_enabled BOOLEAN DEFAULT TRUE,
    inapp_ticket_assigned BOOLEAN DEFAULT TRUE,
    inapp_ticket_updated BOOLEAN DEFAULT TRUE,
    inapp_ticket_commented BOOLEAN DEFAULT TRUE,
    inapp_mentions BOOLEAN DEFAULT TRUE,
    inapp_sound BOOLEAN DEFAULT TRUE,

    -- Push notifications
    push_enabled BOOLEAN DEFAULT FALSE,
    push_token VARCHAR(500),

    -- SMS notifications
    sms_enabled BOOLEAN DEFAULT FALSE,
    sms_phone VARCHAR(20),
    sms_urgent_only BOOLEAN DEFAULT TRUE,

    -- Quiet hours
    quiet_hours_enabled BOOLEAN DEFAULT FALSE,
    quiet_hours_start VARCHAR(10) DEFAULT '22:00',
    quiet_hours_end VARCHAR(10) DEFAULT '08:00',
    quiet_hours_timezone VARCHAR(50) DEFAULT 'UTC',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE UNIQUE INDEX idx_notification_preferences_user ON notification_preferences(user_id);

-- Email Templates
CREATE TABLE email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(500) NOT NULL,
    body_html TEXT NOT NULL,
    body_text TEXT,
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Email Logs
CREATE TABLE email_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    template_id UUID REFERENCES email_templates(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    sent_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_logs_status ON email_logs(status);
CREATE INDEX idx_email_logs_recipient ON email_logs(recipient);
CREATE INDEX idx_email_logs_created ON email_logs(created_at DESC);

-- Notification Batches (for digest emails)
CREATE TABLE notification_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL,
    batch_type VARCHAR(50) NOT NULL, -- DAILY_DIGEST, WEEKLY_REPORT
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    scheduled_for TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    notification_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_batches_user ON notification_batches(user_id);
CREATE INDEX idx_notification_batches_scheduled ON notification_batches(scheduled_for) WHERE status = 'PENDING';

-- Insert default email templates
INSERT INTO email_templates (id, name, subject, body_html, body_text, locale) VALUES
(gen_random_uuid(), 'TICKET_ASSIGNED', 'Ticket #{{ticketNumber}} assigned to you',
'<h2>New Ticket Assignment</h2><p>Ticket <strong>#{{ticketNumber}}</strong> has been assigned to you.</p><p><strong>Subject:</strong> {{ticketTitle}}</p><p><strong>Priority:</strong> {{priority}}</p><p><a href="{{ticketUrl}}">View Ticket</a></p>',
'New Ticket Assignment\n\nTicket #{{ticketNumber}} has been assigned to you.\n\nSubject: {{ticketTitle}}\nPriority: {{priority}}\n\nView Ticket: {{ticketUrl}}',
'en'),

(gen_random_uuid(), 'TICKET_UPDATED', 'Ticket #{{ticketNumber}} updated',
'<h2>Ticket Updated</h2><p>Ticket <strong>#{{ticketNumber}}</strong> has been updated.</p><p><strong>Subject:</strong> {{ticketTitle}}</p><p><strong>Updated by:</strong> {{updatedBy}}</p><p><a href="{{ticketUrl}}">View Ticket</a></p>',
'Ticket Updated\n\nTicket #{{ticketNumber}} has been updated.\n\nSubject: {{ticketTitle}}\nUpdated by: {{updatedBy}}\n\nView Ticket: {{ticketUrl}}',
'en'),

(gen_random_uuid(), 'SLA_WARNING', 'SLA Warning: Ticket #{{ticketNumber}}',
'<h2>SLA Warning</h2><p>Ticket <strong>#{{ticketNumber}}</strong> is approaching SLA breach.</p><p><strong>Time remaining:</strong> {{timeRemaining}}</p><p><a href="{{ticketUrl}}">View Ticket</a></p>',
'SLA Warning\n\nTicket #{{ticketNumber}} is approaching SLA breach.\n\nTime remaining: {{timeRemaining}}\n\nView Ticket: {{ticketUrl}}',
'en'),

(gen_random_uuid(), 'SLA_BREACHED', 'SLA Breached: Ticket #{{ticketNumber}}',
'<h2 style="color: red;">SLA Breached</h2><p>Ticket <strong>#{{ticketNumber}}</strong> has breached its SLA.</p><p><strong>Subject:</strong> {{ticketTitle}}</p><p><a href="{{ticketUrl}}">View Ticket</a></p>',
'SLA BREACHED\n\nTicket #{{ticketNumber}} has breached its SLA.\n\nSubject: {{ticketTitle}}\n\nView Ticket: {{ticketUrl}}',
'en');
