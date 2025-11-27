-- Knowledge Base Schema
-- V1: Initial schema for knowledge service

-- Article Categories
CREATE TABLE article_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(100),
    color VARCHAR(50),
    parent_id UUID REFERENCES article_categories(id) ON DELETE SET NULL,
    sort_order INTEGER DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    project_id VARCHAR(100),
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_article_categories_parent ON article_categories(parent_id);
CREATE INDEX idx_article_categories_project ON article_categories(project_id);
CREATE INDEX idx_article_categories_slug ON article_categories(slug);

-- Articles
CREATE TABLE articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    summary TEXT,
    content TEXT NOT NULL,
    content_html TEXT,
    category_id UUID REFERENCES article_categories(id) ON DELETE SET NULL,
    author_id VARCHAR(100) NOT NULL,
    author_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    view_count BIGINT DEFAULT 0,
    helpful_count BIGINT DEFAULT 0,
    not_helpful_count BIGINT DEFAULT 0,
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    is_featured BOOLEAN DEFAULT FALSE,
    is_internal BOOLEAN DEFAULT FALSE,
    project_id VARCHAR(100),
    meta_title VARCHAR(255),
    meta_description TEXT,
    meta_keywords VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_project ON articles(project_id);
CREATE INDEX idx_articles_published ON articles(published_at);
CREATE INDEX idx_articles_slug ON articles(slug);
CREATE INDEX idx_articles_featured ON articles(is_featured) WHERE is_featured = TRUE;

-- Article Tags
CREATE TABLE article_tags (
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (article_id, tag)
);

CREATE INDEX idx_article_tags_tag ON article_tags(tag);

-- Article Versions (for history)
CREATE TABLE article_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    content_html TEXT,
    changed_by VARCHAR(100) NOT NULL,
    change_summary VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_article_versions_article ON article_versions(article_id);
CREATE UNIQUE INDEX idx_article_versions_unique ON article_versions(article_id, version_number);

-- Article Feedback
CREATE TABLE article_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id VARCHAR(100),
    is_helpful BOOLEAN NOT NULL,
    feedback_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_article_feedback_article ON article_feedback(article_id);

-- Related Articles
CREATE TABLE article_relations (
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    related_article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) DEFAULT 'RELATED',
    sort_order INTEGER DEFAULT 0,
    PRIMARY KEY (article_id, related_article_id)
);

-- Article Attachments
CREATE TABLE article_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    storage_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE INDEX idx_article_attachments_article ON article_attachments(article_id);

-- Full-text search
CREATE INDEX idx_articles_search ON articles USING GIN (to_tsvector('english', title || ' ' || COALESCE(content, '')));
