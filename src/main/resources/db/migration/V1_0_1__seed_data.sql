-- This script inserts 10 dummy users with various roles and types for testing and development.

DO $$
DECLARE
sys_manager_id UUID := uuid_generate_v4();
    admin_id UUID := uuid_generate_v4();
    moderator_id UUID := uuid_generate_v4();
    creator1_id UUID := uuid_generate_v4();
    creator2_id UUID := uuid_generate_v4();
    evaluator_id UUID := uuid_generate_v4();
    student1_id UUID := uuid_generate_v4();
    student2_id UUID := uuid_generate_v4();
    student3_id UUID := uuid_generate_v4();
    multi_role_user_id UUID := uuid_generate_v4();
BEGIN
    -- Insert 10 dummy users
INSERT INTO users (id, email, username, full_name, user_type, enabled) VALUES
                                                                           (sys_manager_id, 'manager@example.com', 'sysmanager', 'System Manager', 'SYSTEM_USER', true),
                                                                           (admin_id, 'admin@example.com', 'adminuser', 'Admin User', 'SYSTEM_USER', true),
                                                                           (moderator_id, 'moderator@example.com', 'moduser', 'Moderator User', 'SYSTEM_USER', true),
                                                                           (creator1_id, 'creator1@example.com', 'creator1', 'Course Creator One', 'WEBSITE_USER', true),
                                                                           (creator2_id, 'creator2@example.com', 'creator2', 'Course Creator Two', 'WEBSITE_USER', true),
                                                                           (evaluator_id, 'evaluator@example.com', 'evaluator', 'Batch Evaluator', 'WEBSITE_USER', true),
                                                                           (student1_id, 'student1@example.com', 'student1', 'Student One', 'WEBSITE_USER', true),
                                                                           (student2_id, 'student2@example.com', 'student2', 'Student Two', 'WEBSITE_USER', true),
                                                                           (student3_id, 'student3@example.com', 'student3', 'Student Three', 'WEBSITE_USER', true),
                                                                           (multi_role_user_id, 'multi.role@example.com', 'multirole', 'Multi Role User', 'WEBSITE_USER', true);

-- Assign roles to the dummy users
INSERT INTO user_roles (user_id, role) VALUES
                                           (sys_manager_id, 'SYSTEM_MANAGER'),
                                           (admin_id, 'SYSTEM_MANAGER'),       -- Another system manager for variety
                                           (moderator_id, 'MODERATOR'),
                                           (creator1_id, 'COURSE_CREATOR'),
                                           (creator2_id, 'COURSE_CREATOR'),
                                           (evaluator_id, 'BATCH_EVALUATOR'),
                                           (student1_id, 'LMS_STUDENT'),
                                           (student2_id, 'LMS_STUDENT'),
                                           (student3_id, 'LMS_STUDENT'),
                                           (multi_role_user_id, 'COURSE_CREATOR'), -- This user has two roles
                                           (multi_role_user_id, 'LMS_STUDENT');
END $$;