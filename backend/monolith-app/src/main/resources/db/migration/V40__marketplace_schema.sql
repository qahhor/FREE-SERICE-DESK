-- Marketplace Modules
CREATE TABLE marketplace_modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    author VARCHAR(255) NOT NULL,
    author_url VARCHAR(500),
    documentation_url VARCHAR(500),
    support_url VARCHAR(500),
    repository_url VARCHAR(500),
    icon TEXT,
    pricing_model VARCHAR(50) NOT NULL DEFAULT 'FREE',
    price DECIMAL(10, 2),
    price_currency VARCHAR(3) DEFAULT 'USD',
    trial_days INTEGER DEFAULT 0,
    latest_version VARCHAR(50),
    minimum_platform_version VARCHAR(50),
    install_count BIGINT DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_verified BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    is_official BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_modules_category ON marketplace_modules(category);
CREATE INDEX idx_modules_status ON marketplace_modules(status);
CREATE INDEX idx_modules_featured ON marketplace_modules(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_modules_official ON marketplace_modules(is_official) WHERE is_official = TRUE;
CREATE INDEX idx_modules_install_count ON marketplace_modules(install_count DESC);
CREATE INDEX idx_modules_rating ON marketplace_modules(average_rating DESC);

-- Module Screenshots
CREATE TABLE module_screenshots (
    module_id UUID NOT NULL REFERENCES marketplace_modules(id) ON DELETE CASCADE,
    screenshot_url TEXT NOT NULL
);

CREATE INDEX idx_module_screenshots_module ON module_screenshots(module_id);

-- Module Tags
CREATE TABLE module_tags (
    module_id UUID NOT NULL REFERENCES marketplace_modules(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL
);

CREATE INDEX idx_module_tags_module ON module_tags(module_id);
CREATE INDEX idx_module_tags_tag ON module_tags(tag);

-- Module Dependencies
CREATE TABLE module_dependencies (
    module_id UUID NOT NULL REFERENCES marketplace_modules(id) ON DELETE CASCADE,
    dependency_module_id VARCHAR(100) NOT NULL
);

CREATE INDEX idx_module_dependencies_module ON module_dependencies(module_id);

-- Module Versions
CREATE TABLE module_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL REFERENCES marketplace_modules(id) ON DELETE CASCADE,
    version VARCHAR(50) NOT NULL,
    release_notes TEXT,
    changelog TEXT,
    download_url TEXT,
    file_size BIGINT,
    checksum_sha256 VARCHAR(64),
    minimum_platform_version VARCHAR(50),
    maximum_platform_version VARCHAR(50),
    configuration_schema TEXT,
    default_configuration TEXT,
    required_permissions TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_stable BOOLEAN DEFAULT TRUE,
    download_count BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(module_id, version)
);

CREATE INDEX idx_module_versions_module ON module_versions(module_id);
CREATE INDEX idx_module_versions_status ON module_versions(status);

-- Module Installations
CREATE TABLE module_installations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    module_id VARCHAR(100) NOT NULL,
    installed_version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INSTALLING',
    configuration JSONB DEFAULT '{}',
    installed_by UUID,
    enabled BOOLEAN DEFAULT TRUE,
    auto_update BOOLEAN DEFAULT FALSE,
    license_key VARCHAR(500),
    license_expires_at TIMESTAMP WITH TIME ZONE,
    trial_started_at TIMESTAMP WITH TIME ZONE,
    trial_expires_at TIMESTAMP WITH TIME ZONE,
    last_health_check TIMESTAMP WITH TIME ZONE,
    health_status VARCHAR(50),
    error_message TEXT,
    usage_stats JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, module_id)
);

CREATE INDEX idx_installations_tenant ON module_installations(tenant_id);
CREATE INDEX idx_installations_module ON module_installations(module_id);
CREATE INDEX idx_installations_status ON module_installations(status);
CREATE INDEX idx_installations_enabled ON module_installations(tenant_id, enabled) WHERE enabled = TRUE;

-- Module Reviews
CREATE TABLE module_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title TEXT,
    comment TEXT,
    installed_version VARCHAR(50),
    helpful_count INTEGER DEFAULT 0,
    reported BOOLEAN DEFAULT FALSE,
    verified_purchase BOOLEAN DEFAULT FALSE,
    author_response TEXT,
    author_response_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(module_id, user_id)
);

CREATE INDEX idx_reviews_module ON module_reviews(module_id);
CREATE INDEX idx_reviews_user ON module_reviews(user_id);
CREATE INDEX idx_reviews_status ON module_reviews(status);
CREATE INDEX idx_reviews_rating ON module_reviews(module_id, rating);

-- Function to update module average rating
CREATE OR REPLACE FUNCTION update_module_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE marketplace_modules
    SET
        average_rating = (
            SELECT COALESCE(AVG(rating), 0)
            FROM module_reviews
            WHERE module_id = NEW.module_id AND status = 'PUBLISHED'
        ),
        review_count = (
            SELECT COUNT(*)
            FROM module_reviews
            WHERE module_id = NEW.module_id AND status = 'PUBLISHED'
        ),
        updated_at = NOW()
    WHERE module_id = NEW.module_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_module_rating
AFTER INSERT OR UPDATE OR DELETE ON module_reviews
FOR EACH ROW EXECUTE FUNCTION update_module_rating();

-- Updated at trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_marketplace_modules_updated_at
    BEFORE UPDATE ON marketplace_modules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_module_installations_updated_at
    BEFORE UPDATE ON module_installations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_module_reviews_updated_at
    BEFORE UPDATE ON module_reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
