-- Asset/CMDB Management Migration
-- Version 5: Complete Asset Management infrastructure

-- =============================================================================
-- Asset Categories
-- =============================================================================
CREATE TABLE IF NOT EXISTS asset_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    color VARCHAR(7) DEFAULT '#6366f1',
    parent_id UUID REFERENCES asset_categories(id),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_asset_cat_parent ON asset_categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_asset_cat_name ON asset_categories(name);

-- =============================================================================
-- Assets (CMDB Configuration Items)
-- =============================================================================
CREATE TABLE IF NOT EXISTS assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_tag VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES asset_categories(id),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    type VARCHAR(30) NOT NULL DEFAULT 'HARDWARE',

    -- Ownership
    owner_id UUID REFERENCES users(id),
    department VARCHAR(100),
    location VARCHAR(200),
    cost_center VARCHAR(50),

    -- Hardware/Software details
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),

    -- Software specific
    version VARCHAR(50),
    license_key VARCHAR(255),
    license_type VARCHAR(50),
    license_expiry DATE,

    -- Financial
    purchase_date DATE,
    purchase_cost DECIMAL(12, 2),
    warranty_expiry DATE,
    depreciation_rate DECIMAL(5, 2),
    current_value DECIMAL(12, 2),

    -- Network (for network devices)
    ip_address VARCHAR(45),
    mac_address VARCHAR(17),
    hostname VARCHAR(255),

    -- Custom fields (JSONB for flexibility)
    custom_fields JSONB,

    -- Audit
    project_id UUID REFERENCES projects(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_assets_tag ON assets(asset_tag);
CREATE INDEX IF NOT EXISTS idx_assets_name ON assets(name);
CREATE INDEX IF NOT EXISTS idx_assets_category ON assets(category_id);
CREATE INDEX IF NOT EXISTS idx_assets_status ON assets(status);
CREATE INDEX IF NOT EXISTS idx_assets_type ON assets(type);
CREATE INDEX IF NOT EXISTS idx_assets_owner ON assets(owner_id);
CREATE INDEX IF NOT EXISTS idx_assets_serial ON assets(serial_number);
CREATE INDEX IF NOT EXISTS idx_assets_hostname ON assets(hostname);
CREATE INDEX IF NOT EXISTS idx_assets_ip ON assets(ip_address);

-- =============================================================================
-- Asset Relationships (for CMDB dependency mapping)
-- =============================================================================
CREATE TABLE IF NOT EXISTS asset_relationships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    child_asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    relationship_type VARCHAR(50) NOT NULL, -- CONTAINS, DEPENDS_ON, CONNECTED_TO, INSTALLED_ON, etc.
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(parent_asset_id, child_asset_id, relationship_type)
);

CREATE INDEX IF NOT EXISTS idx_asset_rel_parent ON asset_relationships(parent_asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_rel_child ON asset_relationships(child_asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_rel_type ON asset_relationships(relationship_type);

-- =============================================================================
-- Asset-Ticket Links (Impact Analysis)
-- =============================================================================
CREATE TABLE IF NOT EXISTS asset_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    impact_type VARCHAR(30) DEFAULT 'AFFECTED', -- AFFECTED, CAUSED_BY, RELATED_TO
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(asset_id, ticket_id)
);

CREATE INDEX IF NOT EXISTS idx_asset_ticket_asset ON asset_tickets(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_ticket_ticket ON asset_tickets(ticket_id);

-- =============================================================================
-- Asset History (Audit Trail)
-- =============================================================================
CREATE TABLE IF NOT EXISTS asset_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id),
    action VARCHAR(50) NOT NULL, -- CREATED, UPDATED, STATUS_CHANGED, ASSIGNED, etc.
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

CREATE INDEX IF NOT EXISTS idx_asset_history_asset ON asset_history(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_history_user ON asset_history(user_id);
CREATE INDEX IF NOT EXISTS idx_asset_history_action ON asset_history(action);
CREATE INDEX IF NOT EXISTS idx_asset_history_date ON asset_history(created_at);

-- =============================================================================
-- Asset Maintenance Schedule
-- =============================================================================
CREATE TABLE IF NOT EXISTS asset_maintenance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id UUID NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    maintenance_type VARCHAR(50) NOT NULL, -- PREVENTIVE, CORRECTIVE, INSPECTION
    scheduled_date DATE NOT NULL,
    completed_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    assigned_to UUID REFERENCES users(id),
    notes TEXT,
    cost DECIMAL(12, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_asset_maint_asset ON asset_maintenance(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_maint_status ON asset_maintenance(status);
CREATE INDEX IF NOT EXISTS idx_asset_maint_date ON asset_maintenance(scheduled_date);

-- =============================================================================
-- Insert default asset categories
-- =============================================================================
INSERT INTO asset_categories (id, name, description, icon, sort_order) VALUES
    (uuid_generate_v4(), 'Hardware', 'Physical equipment and devices', 'computer', 1),
    (uuid_generate_v4(), 'Software', 'Software applications and licenses', 'apps', 2),
    (uuid_generate_v4(), 'Network', 'Network infrastructure', 'router', 3),
    (uuid_generate_v4(), 'Peripherals', 'Monitors, keyboards, mice, etc.', 'devices', 4),
    (uuid_generate_v4(), 'Mobile Devices', 'Phones, tablets, etc.', 'phone_iphone', 5),
    (uuid_generate_v4(), 'Servers', 'Physical and virtual servers', 'dns', 6),
    (uuid_generate_v4(), 'Storage', 'Storage devices and solutions', 'storage', 7),
    (uuid_generate_v4(), 'Printers', 'Printers and scanners', 'print', 8)
ON CONFLICT DO NOTHING;
