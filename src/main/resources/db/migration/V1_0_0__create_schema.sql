-- Migration: 1_create_frappe_lms_schema
-- Date: 2025-08-03

-- UP
-- This section creates all tables, custom ENUM types, and functions for the Frappe Learning LMS.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Custom function to update the 'modified' timestamp
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = now();
RETURN NEW;
END;
$$ language 'plpgsql';

-- 1. ENUMS (Custom types for PostgreSQL)
-- These must be created before the tables that use them.
CREATE TYPE user_type_enum AS ENUM('SYSTEM_USER', 'WEBSITE_USER');
CREATE TYPE user_role_enum AS ENUM('SYSTEM_MANAGER', 'MODERATOR', 'COURSE_CREATOR', 'BATCH_EVALUATOR', 'LMS_STUDENT');
CREATE TYPE course_status_enum AS ENUM('IN_PROGRESS', 'UNDER_REVIEW', 'APPROVED');
CREATE TYPE enrollment_member_type_enum AS ENUM('STUDENT', 'MENTOR', 'STAFF');
CREATE TYPE enrollment_role_enum AS ENUM('MEMBER', 'ADMIN');
CREATE TYPE course_progress_status_enum AS ENUM('COMPLETE', 'PARTIALLY_COMPLETE', 'INCOMPLETE');
CREATE TYPE batch_medium_enum AS ENUM('ONLINE', 'OFFLINE');
CREATE TYPE assignment_type_enum AS ENUM('DOCUMENT', 'PDF', 'URL', 'IMAGE', 'TEXT');
CREATE TYPE assignment_status_enum AS ENUM('PASS', 'FAIL', 'NOT_GRADED', 'NOT_APPLICABLE');
CREATE TYPE quiz_question_type_enum AS ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT', 'NUMBER');
CREATE TYPE programming_exercise_status_enum AS ENUM('PASSED', 'FAILED');
CREATE TYPE certificate_request_status_enum AS ENUM('UPCOMING', 'COMPLETED', 'CANCELLED');
CREATE TYPE job_type_enum AS ENUM('FULL_TIME', 'PART_TIME', 'FREELANCE', 'CONTRACT');
CREATE TYPE job_status_enum AS ENUM('OPEN', 'CLOSED');

-- 2. TABLES
-- BẢNG NGƯỜI DÙNG VÀ PHÂN QUYỀN
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       username VARCHAR(255) UNIQUE,
                       full_name VARCHAR(255) NOT NULL,
                       user_image VARCHAR(255),
                       enabled BOOLEAN DEFAULT TRUE,
                       user_type user_type_enum DEFAULT 'WEBSITE_USER',
                       last_active TIMESTAMPTZ,
                       creation TIMESTAMPTZ DEFAULT NOW(),
                       modified TIMESTAMPTZ DEFAULT NOW(),
                       modified_by UUID
);

CREATE TABLE user_roles (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            user_id UUID NOT NULL,
                            role user_role_enum NOT NULL,
                            creation TIMESTAMPTZ DEFAULT NOW(),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            UNIQUE (user_id, role)
);

-- BẢNG KHÓA HỌC VÀ NỘI DUNG
CREATE TABLE courses (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         title VARCHAR(255) NOT NULL,
                         short_introduction TEXT,
                         description TEXT,
                         image VARCHAR(255),
                         video_link VARCHAR(500),
                         tags VARCHAR(500),
                         category VARCHAR(255),
                         status course_status_enum DEFAULT 'IN_PROGRESS',
                         published BOOLEAN DEFAULT FALSE,
                         published_on DATE,
                         upcoming BOOLEAN DEFAULT FALSE,
                         featured BOOLEAN DEFAULT FALSE,
                         disable_self_learning BOOLEAN DEFAULT FALSE,
                         paid_course BOOLEAN DEFAULT FALSE,
                         course_price DECIMAL(10,2),
                         currency VARCHAR(10),
                         amount_usd DECIMAL(10,2),
                         enable_certification BOOLEAN DEFAULT FALSE,
                         paid_certificate BOOLEAN DEFAULT FALSE,
                         evaluator UUID,
                         enrollments INTEGER DEFAULT 0,
                         lessons INTEGER DEFAULT 0,
                         rating DECIMAL(3,2) DEFAULT 0,
                         creation TIMESTAMPTZ DEFAULT NOW(),
                         modified TIMESTAMPTZ DEFAULT NOW(),
                         modified_by UUID,
                         FOREIGN KEY (evaluator) REFERENCES users(id)
);

CREATE TABLE course_instructors (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    course_id UUID NOT NULL,
                                    instructor_id UUID NOT NULL,
                                    creation TIMESTAMPTZ DEFAULT NOW(),
                                    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                    FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE,
                                    UNIQUE (course_id, instructor_id)
);

CREATE TABLE chapters (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          title VARCHAR(255) NOT NULL,
                          course_id UUID NOT NULL,
                          is_scorm_package BOOLEAN DEFAULT FALSE,
                          scorm_package VARCHAR(255),
                          scorm_package_path TEXT,
                          manifest_file TEXT,
                          launch_file TEXT,
                          creation TIMESTAMPTZ DEFAULT NOW(),
                          modified TIMESTAMPTZ DEFAULT NOW(),
                          modified_by UUID,
                          FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE lessons (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         title VARCHAR(255) NOT NULL,
                         chapter_id UUID NOT NULL,
                         course_id UUID NOT NULL,
                         content TEXT,
                         video_url VARCHAR(500),
                         file_url VARCHAR(500),
                         duration INTEGER,
                         creation TIMESTAMPTZ DEFAULT NOW(),
                         modified TIMESTAMPTZ DEFAULT NOW(),
                         modified_by UUID,
                         FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
                         FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- BẢNG ĐĂNG KÝ VÀ TIẾN ĐỘ
CREATE TABLE enrollments (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             member_id UUID NOT NULL,
                             course_id UUID NOT NULL,
                             member_type enrollment_member_type_enum DEFAULT 'STUDENT',
                             role enrollment_role_enum DEFAULT 'MEMBER',
                             progress DECIMAL(5,2) DEFAULT 0,
                             current_lesson UUID,
                             payment_id UUID,
                             purchased_certificate BOOLEAN DEFAULT FALSE,
                             certificate_id UUID,
                             cohort_id UUID,
                             subgroup_id UUID,
                             batch_old_id UUID,
                             creation TIMESTAMPTZ DEFAULT NOW(),
                             modified TIMESTAMPTZ DEFAULT NOW(),
                             FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                             FOREIGN KEY (current_lesson) REFERENCES lessons(id),
    -- NOTE: 'certificates' table needs to exist before this FK can be created.
    -- Assuming a 'certificates' table will be created later.
    -- FOREIGN KEY (certificate_id) REFERENCES certificates(id),
                             UNIQUE (member_id, course_id)
);

CREATE TABLE course_progress (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 member_id UUID NOT NULL,
                                 lesson_id UUID NOT NULL,
                                 chapter_id UUID NOT NULL,
                                 course_id UUID NOT NULL,
                                 status course_progress_status_enum DEFAULT 'INCOMPLETE',
                                 creation TIMESTAMPTZ DEFAULT NOW(),
                                 modified TIMESTAMPTZ DEFAULT NOW(),
                                 FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                                 FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
                                 FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
                                 FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                 UNIQUE (member_id, lesson_id)
);

-- BẢNG LỚP HỌC (BATCHES)
CREATE TABLE batches (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         start_time TIME NOT NULL,
                         end_time TIME,
                         timezone VARCHAR(50),
                         published BOOLEAN DEFAULT FALSE,
                         allow_self_enrollment BOOLEAN DEFAULT FALSE,
                         certification BOOLEAN DEFAULT FALSE,
                         medium batch_medium_enum DEFAULT 'ONLINE',
                         category VARCHAR(255),
                         seat_count INTEGER,
                         evaluation_end_date DATE,
                         meta_image VARCHAR(255),
                         batch_details TEXT,
                         batch_details_raw TEXT,
                         show_live_class BOOLEAN DEFAULT FALSE,
                         allow_future BOOLEAN DEFAULT FALSE,
                         paid_batch BOOLEAN DEFAULT FALSE,
                         amount DECIMAL(10,2),
                         currency VARCHAR(10),
                         amount_usd DECIMAL(10,2),
                         custom_component TEXT,
                         custom_script TEXT,
                         creation TIMESTAMPTZ DEFAULT NOW(),
                         modified TIMESTAMPTZ DEFAULT NOW(),
                         modified_by UUID
);

CREATE TABLE batch_courses (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               batch_id UUID NOT NULL,
                               course_id UUID NOT NULL,
                               creation TIMESTAMPTZ DEFAULT NOW(),
                               FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
                               FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                               UNIQUE (batch_id, course_id)
);

CREATE TABLE batch_enrollments (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   member_id UUID NOT NULL,
                                   batch_id UUID NOT NULL,
                                   payment_id UUID,
                                   source VARCHAR(255),
                                   confirmation_email_sent BOOLEAN DEFAULT FALSE,
                                   creation TIMESTAMPTZ DEFAULT NOW(),
                                   FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                                   FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    -- NOTE: 'payments' table needs to exist before this FK can be created.
    -- Assuming a 'payments' table will be created later.
    -- FOREIGN KEY (payment_id) REFERENCES payments(id),
                                   UNIQUE (member_id, batch_id)
);

-- BẢNG BÀI TẬP VÀ ĐÁNH GIÁ
CREATE TABLE assignments (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             title VARCHAR(255) NOT NULL,
                             question TEXT NOT NULL,
                             type assignment_type_enum NOT NULL,
                             grade_assignment BOOLEAN DEFAULT TRUE,
                             show_answer BOOLEAN DEFAULT FALSE,
                             answer TEXT,
                             creation TIMESTAMPTZ DEFAULT NOW(),
                             modified TIMESTAMPTZ DEFAULT NOW(),
                             modified_by UUID
);

CREATE TABLE assignment_submissions (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                        assignment_id UUID NOT NULL,
                                        member_id UUID NOT NULL,
                                        evaluator_id UUID,
                                        assignment_attachment VARCHAR(255),
                                        answer TEXT,
                                        status assignment_status_enum DEFAULT 'NOT_GRADED',
                                        comments TEXT,
                                        question TEXT,
                                        course_id UUID,
                                        lesson_id UUID,
                                        creation TIMESTAMPTZ DEFAULT NOW(),
                                        modified TIMESTAMPTZ DEFAULT NOW(),
                                        FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
                                        FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                                        FOREIGN KEY (evaluator_id) REFERENCES users(id),
                                        FOREIGN KEY (course_id) REFERENCES courses(id),
                                        FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

CREATE TABLE quizzes (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         title VARCHAR(255) NOT NULL UNIQUE,
                         lesson_id UUID,
                         course_id UUID,
                         max_attempts INTEGER DEFAULT 0,
                         show_answers BOOLEAN DEFAULT TRUE,
                         show_submission_history BOOLEAN DEFAULT FALSE,
                         total_marks INTEGER DEFAULT 0,
                         passing_percentage INTEGER NOT NULL,
                         duration VARCHAR(50),
                         shuffle_questions BOOLEAN DEFAULT FALSE,
                         limit_questions_to INTEGER,
                         enable_negative_marking BOOLEAN DEFAULT FALSE,
                         marks_to_cut INTEGER DEFAULT 1,
                         creation TIMESTAMPTZ DEFAULT NOW(),
                         modified TIMESTAMPTZ DEFAULT NOW(),
                         modified_by UUID,
                         FOREIGN KEY (lesson_id) REFERENCES lessons(id),
                         FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE quiz_questions (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                quiz_id UUID NOT NULL,
                                question TEXT NOT NULL,
                                type quiz_question_type_enum NOT NULL,
                                options JSONB,
                                correct_answer TEXT,
                                marks INTEGER DEFAULT 1,
                                creation TIMESTAMPTZ DEFAULT NOW(),
                                FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

CREATE TABLE quiz_submissions (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  quiz_id UUID NOT NULL,
                                  member_id UUID NOT NULL,
                                  course_id UUID,
                                  score INTEGER NOT NULL,
                                  score_out_of INTEGER NOT NULL,
                                  percentage INTEGER NOT NULL,
                                  passing_percentage INTEGER NOT NULL,
                                  result JSONB,
                                  creation TIMESTAMPTZ DEFAULT NOW(),
                                  FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
                                  FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                                  FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- BẢNG LẬP TRÌNH
CREATE TABLE programming_exercises (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                       title VARCHAR(255) NOT NULL,
                                       description TEXT,
                                       code TEXT,
                                       answer TEXT,
                                       hints TEXT,
                                       tests TEXT,
                                       image TEXT,
                                       lesson_id UUID,
                                       course_id UUID,
                                       index_num INTEGER,
                                       index_label VARCHAR(255),
                                       creation TIMESTAMPTZ DEFAULT NOW(),
                                       modified TIMESTAMPTZ DEFAULT NOW(),
                                       modified_by UUID,
                                       FOREIGN KEY (lesson_id) REFERENCES lessons(id),
                                       FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE programming_exercise_submissions (
                                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                                  exercise_id UUID NOT NULL,
                                                  member_id UUID NOT NULL,
                                                  status programming_exercise_status_enum,
                                                  code TEXT NOT NULL,
                                                  test_cases JSONB,
                                                  creation TIMESTAMPTZ DEFAULT NOW(),
                                                  modified TIMESTAMPTZ DEFAULT NOW(),
                                                  FOREIGN KEY (exercise_id) REFERENCES programming_exercises(id) ON DELETE CASCADE,
                                                  FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE
);

-- BẢNG CHỨNG CHỈ
CREATE TABLE certificates (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              member_id UUID NOT NULL,
                              course_id UUID,
                              batch_id UUID,
                              evaluator_id UUID,
                              issue_date DATE NOT NULL,
                              expiry_date DATE,
                              template VARCHAR(255) NOT NULL,
                              published BOOLEAN DEFAULT FALSE,
                              creation TIMESTAMPTZ DEFAULT NOW(),
                              modified TIMESTAMPTZ DEFAULT NOW(),
                              modified_by UUID,
                              FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                              FOREIGN KEY (course_id) REFERENCES courses(id),
                              FOREIGN KEY (batch_id) REFERENCES batches(id),
                              FOREIGN KEY (evaluator_id) REFERENCES users(id)
);

CREATE TABLE certificate_requests (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      member_id UUID NOT NULL,
                                      course_id UUID NOT NULL,
                                      batch_id UUID,
                                      status certificate_request_status_enum DEFAULT 'UPCOMING',
                                      creation TIMESTAMPTZ DEFAULT NOW(),
                                      modified TIMESTAMPTZ DEFAULT NOW(),
                                      FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                                      FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
                                      FOREIGN KEY (batch_id) REFERENCES batches(id)
);

-- BẢNG THANH TOÁN
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          member_id UUID NOT NULL,
                          billing_name VARCHAR(255) NOT NULL,
                          source VARCHAR(255),
                          payment_for_document_type VARCHAR(255),
                          payment_for_document VARCHAR(255),
                          payment_received BOOLEAN DEFAULT FALSE,
                          payment_for_certificate BOOLEAN DEFAULT FALSE,
                          currency VARCHAR(10) NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          amount_with_gst DECIMAL(10,2),
                          order_id VARCHAR(255),
                          payment_id VARCHAR(255),
                          address_id VARCHAR(255),
                          gstin VARCHAR(255),
                          pan VARCHAR(255),
                          creation TIMESTAMPTZ DEFAULT NOW(),
                          modified TIMESTAMPTZ DEFAULT NOW(),
                          FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Update the foreign key in `enrollments` and `batch_enrollments` after `certificates` and `payments` tables are created.
ALTER TABLE enrollments ADD CONSTRAINT fk_enrollments_certificate FOREIGN KEY (certificate_id) REFERENCES certificates(id);
ALTER TABLE batch_enrollments ADD CONSTRAINT fk_batch_enrollments_payment FOREIGN KEY (payment_id) REFERENCES payments(id);

-- BẢNG VIỆC LÀM
CREATE TABLE job_opportunities (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   job_title VARCHAR(255) NOT NULL,
                                   location VARCHAR(255) NOT NULL,
                                   country VARCHAR(255) NOT NULL,
                                   type job_type_enum DEFAULT 'FULL_TIME',
                                   status job_status_enum DEFAULT 'OPEN',
                                   disabled BOOLEAN DEFAULT FALSE,
                                   company_name VARCHAR(255) NOT NULL,
                                   company_website VARCHAR(255) NOT NULL,
                                   company_logo VARCHAR(255) NOT NULL,
                                   company_email_address VARCHAR(255) NOT NULL,
                                   description TEXT NOT NULL,
                                   creation TIMESTAMPTZ DEFAULT NOW(),
                                   modified TIMESTAMPTZ DEFAULT NOW(),
                                   modified_by UUID
);

CREATE TABLE job_applications (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  user_id UUID NOT NULL,
                                  job_id UUID NOT NULL,
                                  resume VARCHAR(255) NOT NULL,
                                  creation TIMESTAMPTZ DEFAULT NOW(),
                                  modified TIMESTAMPTZ DEFAULT NOW(),
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                  FOREIGN KEY (job_id) REFERENCES job_opportunities(id) ON DELETE CASCADE,
                                  UNIQUE (user_id, job_id)
);

-- BẢNG THỐNG KÊ VÀ THEO DÕI
-- NOTE: The 'live_classes' table is referenced but not defined in your markdown.
-- Assuming a placeholder table for now.
CREATE TABLE IF NOT EXISTS live_classes (
                                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    -- ... other columns
    creation TIMESTAMPTZ DEFAULT NOW()
    );

CREATE TABLE video_watch_duration (
                                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                      lesson_id UUID NOT NULL,
                                      chapter_id UUID,
                                      course_id UUID,
                                      member_id UUID NOT NULL,
                                      source VARCHAR(255) NOT NULL,
                                      watch_time VARCHAR(255) NOT NULL,
                                      creation TIMESTAMPTZ DEFAULT NOW(),
                                      FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
                                      FOREIGN KEY (chapter_id) REFERENCES chapters(id),
                                      FOREIGN KEY (course_id) REFERENCES courses(id),
                                      FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE live_class_participants (
                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                         live_class_id UUID NOT NULL,
                                         member_id UUID NOT NULL,
                                         joined_at TIMESTAMPTZ NOT NULL,
                                         left_at TIMESTAMPTZ NOT NULL,
                                         duration INTEGER NOT NULL,
                                         creation TIMESTAMPTZ DEFAULT NOW(),
                                         FOREIGN KEY (live_class_id) REFERENCES live_classes(id) ON DELETE CASCADE,
                                         FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE
);

-- BẢNG CẤU HÌNH VÀ THIẾT LẬP
CREATE TABLE lms_settings (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              default_home VARCHAR(255),
                              send_calendar_invite_for_evaluations BOOLEAN DEFAULT FALSE,
                              persona_captured BOOLEAN DEFAULT FALSE,
                              allow_guest_access BOOLEAN DEFAULT FALSE,
                              enable_learning_paths BOOLEAN DEFAULT FALSE,
                              prevent_skipping_videos BOOLEAN DEFAULT FALSE,
                              unsplash_access_key VARCHAR(255),
                              livecode_url VARCHAR(255),
                              show_day_view BOOLEAN DEFAULT FALSE,
                              show_dashboard BOOLEAN DEFAULT TRUE,
                              show_courses BOOLEAN DEFAULT TRUE,
                              show_students BOOLEAN DEFAULT TRUE,
                              show_assessments BOOLEAN DEFAULT TRUE,
                              show_live_class BOOLEAN DEFAULT TRUE,
                              show_discussions BOOLEAN DEFAULT TRUE,
                              show_emails BOOLEAN DEFAULT TRUE,
                              user_category VARCHAR(255),
                              disable_signup BOOLEAN DEFAULT FALSE,
                              custom_signup_content TEXT,
                              payment_gateway VARCHAR(255),
                              default_currency VARCHAR(10),
                              exception_country VARCHAR(255),
                              apply_gst BOOLEAN DEFAULT FALSE,
                              show_usd_equivalent BOOLEAN DEFAULT FALSE,
                              apply_rounding BOOLEAN DEFAULT FALSE,
                              no_payments_app BOOLEAN DEFAULT FALSE,
                              certification_template VARCHAR(255),
                              batch_confirmation_template VARCHAR(255),
                              payment_reminder_template VARCHAR(255),
                              meta_description TEXT,
                              meta_image VARCHAR(255),
                              meta_keywords TEXT,
                              creation TIMESTAMPTZ DEFAULT NOW(),
                              modified TIMESTAMPTZ DEFAULT NOW(),
                              modified_by UUID
);

-- BẢNG BỔ SUNG
CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            creation TIMESTAMPTZ DEFAULT NOW(),
                            modified TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE sources (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         source VARCHAR(255) NOT NULL UNIQUE,
                         creation TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE badges (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        image VARCHAR(255),
                        criteria JSONB,
                        creation TIMESTAMPTZ DEFAULT NOW(),
                        modified TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE badge_assignments (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   badge_id UUID NOT NULL,
                                   member_id UUID NOT NULL,
                                   assigned_by UUID,
                                   assigned_date TIMESTAMPTZ DEFAULT NOW(),
                                   FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
                                   FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
                                   FOREIGN KEY (assigned_by) REFERENCES users(id),
                                   UNIQUE (badge_id, member_id)
);


-- 3. TRIGGERS
-- A trigger is the standard way to handle 'ON UPDATE CURRENT_TIMESTAMP' in PostgreSQL.
-- We attach this trigger to every table that has a 'modified' column.

CREATE TRIGGER users_modified_timestamp
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER courses_modified_timestamp
    BEFORE UPDATE ON courses
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER chapters_modified_timestamp
    BEFORE UPDATE ON chapters
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER lessons_modified_timestamp
    BEFORE UPDATE ON lessons
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER enrollments_modified_timestamp
    BEFORE UPDATE ON enrollments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER course_progress_modified_timestamp
    BEFORE UPDATE ON course_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER batches_modified_timestamp
    BEFORE UPDATE ON batches
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER assignments_modified_timestamp
    BEFORE UPDATE ON assignments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER assignment_submissions_modified_timestamp
    BEFORE UPDATE ON assignment_submissions
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER quizzes_modified_timestamp
    BEFORE UPDATE ON quizzes
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER programming_exercises_modified_timestamp
    BEFORE UPDATE ON programming_exercises
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER programming_exercise_submissions_modified_timestamp
    BEFORE UPDATE ON programming_exercise_submissions
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER certificates_modified_timestamp
    BEFORE UPDATE ON certificates
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER certificate_requests_modified_timestamp
    BEFORE UPDATE ON certificate_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER payments_modified_timestamp
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER job_opportunities_modified_timestamp
    BEFORE UPDATE ON job_opportunities
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER job_applications_modified_timestamp
    BEFORE UPDATE ON job_applications
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER lms_settings_modified_timestamp
    BEFORE UPDATE ON lms_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER categories_modified_timestamp
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER badges_modified_timestamp
    BEFORE UPDATE ON badges
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();
