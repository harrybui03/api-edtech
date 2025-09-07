-- This migration creates tables for tags and labels, which can be associated with various entities.

-- 1. CREATE ENUM TYPE for identifying the entity type
CREATE TYPE entity_type_enum AS ENUM ('COURSE', 'BLOG');

-- 2. CREATE TAGS TABLE
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type entity_type_enum NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. CREATE LABELS TABLE
CREATE TABLE labels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type entity_type_enum NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. ADD INDEXES for performance on lookups
CREATE INDEX idx_tags_on_entity ON tags (entity_id, entity_type);
CREATE INDEX idx_labels_on_entity ON labels (entity_id, entity_type);
