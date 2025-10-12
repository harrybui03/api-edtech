-- 1. CREATE REVIEWS TABLE
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    creation TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_by UUID,
    
    -- Unique constraint: one review per student per course
    CONSTRAINT unique_student_course_review UNIQUE (course_id, student_id)
);

-- 2. CREATE COMMENTS TABLE
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    parent_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    upvotes INTEGER NOT NULL DEFAULT 0,
    downvotes INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creation TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    modified_by UUID
);

-- 3. CREATE COMMENT_VOTES TABLE
CREATE TABLE comment_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_id UUID NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vote_type BOOLEAN NOT NULL, -- true = UPVOTE, false = DOWNVOTE
    creation TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Unique constraint: one vote per user per comment
    CONSTRAINT unique_user_comment_vote UNIQUE (comment_id, user_id)
);

-- 4. ADD INDEXES for performance
CREATE INDEX idx_reviews_course_approved ON reviews (course_id, is_approved);
CREATE INDEX idx_reviews_student ON reviews (student_id);
CREATE INDEX idx_reviews_creation ON reviews (creation DESC);

CREATE INDEX idx_comments_lesson ON comments (lesson_id);
CREATE INDEX idx_comments_parent ON comments (parent_id);
CREATE INDEX idx_comments_author ON comments (author_id);
CREATE INDEX idx_comments_creation ON comments (creation DESC);
CREATE INDEX idx_comments_not_deleted ON comments (is_deleted) WHERE is_deleted = FALSE;

CREATE INDEX idx_comment_votes_comment ON comment_votes (comment_id);
CREATE INDEX idx_comment_votes_user ON comment_votes (user_id);
CREATE INDEX idx_comment_votes_type ON comment_votes (comment_id, vote_type);

-- 5. ADD TRIGGERS for updated_at timestamps
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_reviews_modified 
    BEFORE UPDATE ON reviews 
    FOR EACH ROW 
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_comments_modified 
    BEFORE UPDATE ON comments 
    FOR EACH ROW 
    EXECUTE FUNCTION update_modified_column();
