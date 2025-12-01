-- Achievements Definition
CREATE TABLE IF NOT EXISTS achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    achievement_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon VARCHAR(50) NOT NULL,
    achievement_type VARCHAR(50) NOT NULL,
    earn_condition JSONB,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_achievements_active ON achievements(is_active);
CREATE INDEX IF NOT EXISTS idx_achievements_type ON achievements(achievement_type);

-- User Achievements (earned achievements)
CREATE TABLE IF NOT EXISTS user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    achievement_id UUID NOT NULL REFERENCES achievements(id),
    earned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, achievement_id)
);

CREATE INDEX IF NOT EXISTS idx_user_achievements_user ON user_achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_user_achievements_earned ON user_achievements(earned_at);

-- Seed default achievements
INSERT INTO achievements (achievement_id, name, description, icon, achievement_type, earn_condition, display_order) VALUES
('first_steps', 'First Steps', 'Completed your first onboarding step. Welcome aboard!', 'rocket_launch', 'FIRST_STEP', 
 '{"type": "step_completed", "count": 1}', 1),

('quick_learner', 'Quick Learner', 'Completed the entire onboarding process. You''re ready to go!', 'school', 'QUICK_LEARNER', 
 '{"type": "onboarding_completed"}', 2),

('explorer', 'Explorer', 'Viewed all guided tours and explored every section.', 'explore', 'EXPLORER', 
 '{"type": "all_tours_completed"}', 3)
ON CONFLICT (achievement_id) DO NOTHING;

-- Update trigger for achievements
CREATE OR REPLACE TRIGGER update_achievements_updated_at
    BEFORE UPDATE ON achievements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
