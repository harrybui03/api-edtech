-- Final Consolidated Schema for Frappe Learning LMS

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Custom function to update the 'modified' timestamp
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = now();
    RETURN NEW;
END;
$$ language 'plpgsql';


-- Table for Users and Roles
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    user_image VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    user_type VARCHAR(50) DEFAULT 'WEBSITE_USER',
    last_active TIMESTAMPTZ,
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    creation TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (user_id, role)
);

-- Table for Courses and Content
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    short_introduction TEXT,
    description TEXT,
    image VARCHAR(255),
    video_link VARCHAR(500),
    status VARCHAR(50) DEFAULT 'IN_PROGRESS',
    paid_course BOOLEAN DEFAULT FALSE,
    course_price DECIMAL(10,2),
    selling_price DECIMAL(10, 2),
    currency VARCHAR(10),
    amount_usd DECIMAL(10,2),
    enrollments INTEGER DEFAULT 0,
    lessons INTEGER DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0,
    language VARCHAR(50),
    target_audience TEXT,
    skill_level TEXT,
    learner_profile_desc TEXT,
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE course_instructors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    instructor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    creation TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (course_id, instructor_id)
);

CREATE TABLE chapters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    summary TEXT,
    "position" INTEGER,
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

-- Tables for Quizzes
CREATE TABLE quizzes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL UNIQUE,
    show_answers BOOLEAN DEFAULT TRUE,
    show_submission_history BOOLEAN DEFAULT FALSE,
    total_marks INTEGER DEFAULT 0,
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    chapter_id UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    quiz_id UUID REFERENCES quizzes(id) ON DELETE CASCADE,
    content TEXT,
    video_url VARCHAR(500),
    file_url VARCHAR(500),
    "position" INTEGER,
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    modified_by UUID
);

-- Table for Enrollments and Progress
CREATE TABLE enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    member_type VARCHAR(50) DEFAULT 'STUDENT',
    role VARCHAR(50) DEFAULT 'MEMBER',
    progress DECIMAL(5,2) DEFAULT 0,
    current_lesson UUID REFERENCES lessons(id),
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (member_id, course_id)
);

CREATE TABLE course_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    member_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    chapter_id UUID NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'INCOMPLETE' CHECK (status IN ('COMPLETE', 'PARTIALLY_COMPLETE', 'INCOMPLETE')),
    creation TIMESTAMPTZ DEFAULT NOW(),
    modified TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (member_id, lesson_id)
);

CREATE TABLE quiz_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    explanation TEXT,
    options JSONB,
    correct_answer TEXT,
    marks INTEGER DEFAULT 1,
    creation TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE quiz_submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    member_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    score INTEGER NOT NULL,
    percentage INTEGER NOT NULL,
    result JSONB,
    creation TIMESTAMPTZ DEFAULT NOW()
);

-- Tables for Tags and Labels
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE labels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    entity_id UUID NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_tags_on_entity ON tags (entity_id, entity_type);
CREATE INDEX idx_labels_on_entity ON labels (entity_id, entity_type);
