-- V2: Add user_email column to notifications for direct email sending
-- This allows the notification service to send emails without calling user service

ALTER TABLE notifications ADD COLUMN user_email VARCHAR(255);

CREATE INDEX idx_notifications_user_email ON notifications(user_email) WHERE user_email IS NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN notifications.user_email IS 'Direct email address for sending notifications. Populated from calling service.';
