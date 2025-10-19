-- This migration seeds the database with a large, interconnected set of sample data
-- for courses, chapters, lessons, quizzes, and enrollments to facilitate development and testing.
-- This script has been updated to be compatible with the V1_0_0 consolidated schema.

DO $$
DECLARE
    -- User IDs
    instructor_id UUID;
    student1_id UUID;
    student2_id UUID;
    student3_id UUID;

    -- Course IDs (20 courses)
    course_ids UUID[] := array_agg(uuid_generate_v4()) FROM generate_series(1, 20);

    -- Chapter IDs (4 chapters per course for the first 5 courses)
    chapter_ids UUID[] := array_agg(uuid_generate_v4()) FROM generate_series(1, 20);

    -- Lesson IDs (2 lessons per chapter for the first 20 chapters)
    lesson_ids UUID[] := array_agg(uuid_generate_v4()) FROM generate_series(1, 40);

    -- Quiz IDs (1 quiz for the first 20 lessons)
    quiz_ids UUID[] := array_agg(uuid_generate_v4()) FROM generate_series(1, 20);

    -- Quiz Question IDs
    question_ids UUID[] := array_agg(uuid_generate_v4()) FROM generate_series(1, 40);

BEGIN
    -- 1. GET USER IDs FROM PREVIOUS MIGRATION
    SELECT id INTO instructor_id FROM users WHERE email = 'creator1@example.com';
    SELECT id INTO student1_id FROM users WHERE email = 'student1@example.com';
    SELECT id INTO student2_id FROM users WHERE email = 'student2@example.com';
    SELECT id INTO student3_id FROM users WHERE email = 'student3@example.com';

    -- 2. SEED COURSES (20 records)
    -- Note: 'published' column is removed, using 'status' instead.
INSERT INTO courses (
    id, title, slug, short_introduction, description,
    image, video_link, status, paid_course, course_price,
    selling_price, currency, amount_usd, enrollments,
    lessons, rating, language, target_audience,
    skill_level, learner_profile_desc
) VALUES
      (course_ids[1], 'Mastering Spring Boot 3', 'mastering-spring-boot-3', 'A comprehensive guide to building modern applications.', 'Covers dependency injection, microservices, security, and testing.', 'spring-boot-3.jpg', NULL, 'PUBLISHED', TRUE, 2500.00, 2200.00, 'VND', 2200.00, 1500, 45, 4.85, 'English', 'Backend Developers', 'Advanced', 'Professionals looking to master the latest Spring Boot features.'),
      (course_ids[2], 'Introduction to PostgreSQL', 'introduction-to-postgresql', 'Learn the fundamentals of the world''s most advanced open source database.', 'SQL basics, data types, and simple queries for beginners.', 'postgresql-intro.jpg', NULL, 'PUBLISHED', TRUE, 2000.00, 1800.00, 'VND', 1800.00, 850, 20, 4.60, 'English', 'Data Analysts and Beginners', 'Beginner', 'Anyone starting their journey with SQL and relational databases.'),
      (course_ids[3], 'Advanced Docker and Kubernetes', 'advanced-docker-kubernetes', 'Deploy and manage containerized applications at scale.', 'Deep dive into orchestration, networking, and storage.', 'docker-k8s.jpg', NULL, 'PUBLISHED', TRUE, 3000.00, 2800.00, 'VND', 2800.00, 1200, 60, 4.90, 'English', 'DevOps Engineers', 'Expert', 'Experienced users seeking to deploy and manage large-scale systems.'),
      (course_ids[4], 'React for Beginners', 'react-for-beginners', 'Build modern, interactive user interfaces with React.', 'Learn components, state, props, and hooks from scratch.', 'react-beginners.jpg', NULL, 'PUBLISHED', TRUE, 2300.00, 2000.00, 'VND', 2000.00, 2100, 35, 4.75, 'English', 'Front-end Developers', 'Beginner', 'Aspiring web developers and designers who want to learn React.'),
      (course_ids[5], 'Data Structures and Algorithms in Java', 'data-structures-algorithms-java', 'Master the essential concepts for coding interviews.', 'Covers arrays, linked lists, trees, graphs, and sorting algorithms.', 'dsa-java.jpg', NULL, 'PUBLISHED', TRUE, 2400.00, 2100.00, 'VND', 2100.00, 900, 50, 4.80, 'English', 'Software Engineers', 'Intermediate', 'Individuals preparing for technical interviews or strengthening their fundamentals.'),
      (course_ids[6], 'Building RESTful APIs with Node.js', 'restful-apis-nodejs', 'Create fast and scalable backend services.', 'Using Express.js, MongoDB, and best practices.', 'nodejs-api.jpg', NULL, 'DRAFT', TRUE, 2600.00, 2300.00, 'VND', 2300.00, 0, 0, 0.00, 'English', 'Backend Developers', 'Intermediate', 'Developers who want to build modern, production-ready APIs.'),
      (course_ids[7], 'Machine Learning with Python', 'machine-learning-python', 'An introduction to practical machine learning.', 'Using Scikit-learn, Pandas, and NumPy.', 'ml-python.jpg', NULL, 'DRAFT', TRUE, 2900.00, 2700.00, 'VND', 2700.00, 0, 0, 0.00, 'English', 'Data Scientists', 'Intermediate', 'Anyone looking to apply machine learning models to real-world data.'),
      (course_ids[8], 'CSS Grid and Flexbox', 'css-grid-flexbox', 'Modern CSS layout techniques explained.', 'Build complex, responsive layouts with ease.', 'css-layout.jpg', NULL, 'DRAFT', TRUE, 2000.00, 1800.00, 'VND', 1800.00, 0, 0, 0.00, 'English', 'Front-end Developers', 'Beginner', 'Web developers struggling with traditional layout methods.'),
      (course_ids[9], 'Go (Golang) Programming', 'go-programming-basics', 'Learn the basics of Google''s Go language.', 'Concurrency, channels, and building simple web servers.', 'golang-basics.jpg', NULL, 'DRAFT', TRUE, 2500.00, 2200.00, 'VND', 2200.00, 0, 0, 0.00, 'English', 'System Engineers', 'Beginner', 'Developers looking to learn a fast, concurrent, and modern language.'),
      (course_ids[10], 'Introduction to Cloud Computing', 'intro-to-cloud-computing', 'Understand the fundamentals of AWS, Azure, and GCP.', 'Virtual machines, storage, and serverless computing.', 'cloud-intro.jpg', NULL, 'DRAFT', TRUE, 2200.00, 2000.00, 'VND', 2000.00, 0, 0, 0.00, 'English', 'IT Professionals', 'Beginner', 'Anyone who needs a solid understanding of cloud service models and providers.'),
      (course_ids[11], 'Vue.js 3 Fundamentals', 'vuejs-3-fundamentals', 'Build reactive front-end applications with Vue.', 'Composition API, Vuex, and Vue Router.', 'vuejs-3.jpg', NULL, 'DRAFT', TRUE, 2500.00, 2200.00, 'VND', 2200.00, 0, 0, 0.00, 'English', 'Front-end Developers', 'Intermediate', 'Developers with basic JavaScript knowledge ready to learn a reactive framework.'),
      (course_ids[12], 'Advanced SQL Techniques', 'advanced-sql-techniques', 'Master window functions, CTEs, and query optimization.', 'For data analysts and backend developers.', 'advanced-sql.jpg', NULL, 'DRAFT', TRUE, 2800.00, 2600.00, 'VND', 2600.00, 0, 0, 0.00, 'English', 'Data Analysts and Backend Developers', 'Advanced', 'Users of SQL who want to write more efficient and complex queries.'),
      (course_ids[13], 'Test-Driven Development (TDD) in C#', 'tdd-in-csharp', 'Write robust and maintainable code with TDD.', 'Using NUnit and Moq.', 'tdd-csharp.jpg', NULL, 'DRAFT', TRUE, 2400.00, 2100.00, 'VND', 2100.00, 0, 0, 0.00, 'English', 'Software Engineers', 'Intermediate', 'C# developers aiming to improve code quality and testing practices.'),
      (course_ids[14], 'Introduction to UI/UX Design', 'intro-to-ui-ux-design', 'Principles of user-centric design.', 'Wireframing, prototyping, and user testing.', 'ui-ux-intro.jpg', NULL, 'DRAFT', TRUE, 2200.00, 2000.00, 'VND', 2000.00, 0, 0, 0.00, 'English', 'Designers and Developers', 'Beginner', 'Anyone interested in the process of creating user-friendly digital products.'),
      (course_ids[15], 'Cybersecurity Fundamentals', 'cybersecurity-fundamentals', 'Protecting systems from common threats.', 'Network security, cryptography, and ethical hacking.', 'cybersecurity.jpg', NULL, 'DRAFT', TRUE, 2900.00, 2700.00, 'VND', 2700.00, 0, 0, 0.00, 'English', 'IT Professionals', 'Intermediate', 'Individuals who want a foundational understanding of digital security.'),
      (course_ids[16], 'GraphQL for API Development', 'graphql-for-api-dev', 'Build flexible and efficient APIs with GraphQL.', 'Compared to REST, with Apollo Server.', 'graphql-api.jpg', NULL, 'DRAFT', TRUE, 2500.00, 2200.00, 'VND', 2200.00, 0, 0, 0.00, 'English', 'Backend Developers', 'Intermediate', 'Developers transitioning from REST to modern API technologies.'),
      (course_ids[17], 'Getting Started with Rust', 'getting-started-with-rust', 'Learn the basics of the Rust programming language.', 'Ownership, borrowing, and lifetimes.', 'rust-basics.jpg', NULL, 'DRAFT', TRUE, 2400.00, 2100.00, 'VND', 2100.00, 0, 0, 0.00, 'English', 'Systems Programmers', 'Beginner', 'Developers interested in high-performance, safe systems programming.'),
      (course_ids[18], 'DevOps CI/CD with Jenkins', 'devops-cicd-jenkins', 'Automate your build and deployment pipeline.', 'Jenkinsfiles, plugins, and best practices.', 'jenkins-cicd.jpg', NULL, 'DRAFT', TRUE, 2800.00, 2600.00, 'VND', 2600.00, 0, 0, 0.00, 'English', 'DevOps Engineers', 'Intermediate', 'Professionals looking to implement and manage automated delivery pipelines.'),
      (course_ids[19], 'Mobile App Development with Flutter', 'mobile-app-dev-flutter', 'Build cross-platform apps from a single codebase.', 'Dart, widgets, and state management.', 'flutter-mobile.jpg', NULL, 'DRAFT', TRUE, 2900.00, 2700.00, 'VND', 2700.00, 0, 0, 0.00, 'English', 'Mobile Developers', 'Intermediate', 'Developers aiming to build native-quality apps for iOS and Android quickly.'),
      (course_ids[20], 'The Complete Guide to Web Scraping', 'complete-guide-web-scraping', 'Extract data from websites using Python.', 'Beautiful Soup, Scrapy, and handling dynamic sites.', 'web-scraping.jpg', NULL, 'DRAFT', TRUE, 2500.00, 2200.00, 'VND', 2200.00, 0, 0, 0.00, 'English', 'Data Engineers', 'Intermediate', 'Anyone who needs to collect and process data from the public web.');

    -- 3. SEED COURSE INSTRUCTORS (20 records)
    INSERT INTO course_instructors (course_id, instructor_id)
    SELECT id, instructor_id FROM courses;

    -- 4. SEED CHAPTERS (20 records)
    -- Note: Adding position column for proper ordering.
    INSERT INTO chapters (id, course_id, title, summary, "position") VALUES
    (chapter_ids[1], course_ids[1], 'Chapter 1: Spring Core', 'The fundamentals of Spring.', 1),
    (chapter_ids[2], course_ids[1], 'Chapter 2: Web Layer', 'Building web applications.', 2),
    (chapter_ids[3], course_ids[1], 'Chapter 3: Data Access', 'Connecting to databases.', 3),
    (chapter_ids[4], course_ids[1], 'Chapter 4: Security', 'Securing your application.', 4),
    (chapter_ids[5], course_ids[2], 'Chapter 1: SQL Basics', 'SELECT, FROM, WHERE.', 1),
    (chapter_ids[6], course_ids[2], 'Chapter 2: Joins', 'INNER, LEFT, RIGHT joins.', 2),
    (chapter_ids[7], course_ids[2], 'Chapter 3: Aggregation', 'GROUP BY, COUNT, SUM.', 3),
    (chapter_ids[8], course_ids[2], 'Chapter 4: Data Types', 'Understanding text, numbers, and dates.', 4),
    (chapter_ids[9], course_ids[3], 'Chapter 1: Docker Basics', 'Images and Containers.', 1),
    (chapter_ids[10], course_ids[3], 'Chapter 2: Docker Compose', 'Multi-container applications.', 2),
    (chapter_ids[11], course_ids[3], 'Chapter 3: Kubernetes Intro', 'Pods, Services, Deployments.', 3),
    (chapter_ids[12], course_ids[3], 'Chapter 4: Helm Charts', 'Packaging Kubernetes applications.', 4),
    (chapter_ids[13], course_ids[4], 'Chapter 1: JSX and Components', 'Building blocks of React.', 1),
    (chapter_ids[14], course_ids[4], 'Chapter 2: State and Props', 'Managing data flow.', 2),
    (chapter_ids[15], course_ids[4], 'Chapter 3: Handling Events', 'User interactions.', 3),
    (chapter_ids[16], course_ids[4], 'Chapter 4: Hooks', 'useState and useEffect.', 4),
    (chapter_ids[17], course_ids[5], 'Chapter 1: Arrays & Strings', 'Fundamental data structures.', 1),
    (chapter_ids[18], course_ids[5], 'Chapter 2: Linked Lists', 'Singly and Doubly linked lists.', 2),
    (chapter_ids[19], course_ids[5], 'Chapter 3: Trees', 'Binary Search Trees.', 3),
    (chapter_ids[20], course_ids[5], 'Chapter 4: Sorting', 'Bubble, Merge, and Quick sort.', 4);

    -- 5. SEED QUIZZES (20 records) - Create quizzes first
    -- Note: 'lesson_id', 'course_id', 'passing_percentage' columns are removed.
    INSERT INTO quizzes (id, title) VALUES
    (quiz_ids[1], 'Quiz: Spring IoC'), (quiz_ids[2], 'Quiz: Bean Lifecycle'),
    (quiz_ids[3], 'Quiz: @RestController'), (quiz_ids[4], 'Quiz: @RequestMapping'),
    (quiz_ids[5], 'Quiz: JPA Basics'), (quiz_ids[6], 'Quiz: Spring Data'),
    (quiz_ids[7], 'Quiz: Basic Auth'), (quiz_ids[8], 'Quiz: JWT Auth'),
    (quiz_ids[9], 'Quiz: SELECT Statement'), (quiz_ids[10], 'Quiz: WHERE Clause'),
    (quiz_ids[11], 'Quiz: INNER JOIN'), (quiz_ids[12], 'Quiz: LEFT JOIN'),
    (quiz_ids[13], 'Quiz: COUNT()'), (quiz_ids[14], 'Quiz: GROUP BY'),
    (quiz_ids[15], 'Quiz: Text Types'), (quiz_ids[16], 'Quiz: Numeric Types'),
    (quiz_ids[17], 'Quiz: Containers'), (quiz_ids[18], 'Quiz: Dockerfiles'),
    (quiz_ids[19], 'Quiz: Docker Compose'), (quiz_ids[20], 'Quiz: K8s Pods');

    -- 6. SEED LESSONS (40 records)
    -- Note: Each lesson can only have ONE of: videoUrl, content, or quizId
INSERT INTO lessons (id, chapter_id, course_id, title, slug, content, "position", quiz_id, video_url) VALUES
-- Lessons 1-8: Content only (Original content retained, quiz_id/video_url explicitly NULL)
(lesson_ids[1], chapter_ids[1], course_ids[1], 'Lesson 1.1: Inversion of Control', 'spring-ioc', 'Content about IoC.', 1, NULL, NULL),
(lesson_ids[2], chapter_ids[1], course_ids[1], 'Lesson 1.2: The Bean Lifecycle', 'spring-bean-lifecycle', 'Content about beans.', 2, NULL, NULL),
(lesson_ids[3], chapter_ids[2], course_ids[1], 'Lesson 2.1: @RestController', 'spring-restcontroller', 'Content about controllers.', 1, NULL, NULL),
(lesson_ids[4], chapter_ids[2], course_ids[1], 'Lesson 2.2: @RequestMapping', 'spring-requestmapping', 'Content about mappings.', 2, NULL, NULL),
(lesson_ids[5], chapter_ids[3], course_ids[1], 'Lesson 3.1: JPA and Hibernate', 'spring-jpa', 'Content about JPA.', 1, NULL, NULL),
(lesson_ids[6], chapter_ids[3], course_ids[1], 'Lesson 3.2: Spring Data Repositories', 'spring-data-repo', 'Content about repositories.', 2, NULL, NULL),
(lesson_ids[7], chapter_ids[4], course_ids[1], 'Lesson 4.1: Basic Authentication', 'spring-security-basic', 'Content about basic auth.', 1, NULL, NULL),
(lesson_ids[8], chapter_ids[4], course_ids[1], 'Lesson 4.2: JWT Authentication', 'spring-security-jwt', 'Content about JWT.', 2, NULL, NULL),
-- Lessons 9-16: Content only (Original content retained, quiz_id/video_url explicitly NULL)
(lesson_ids[9], chapter_ids[5], course_ids[2], 'Lesson 1.1: The SELECT statement', 'sql-select', 'How to query data.', 1, NULL, NULL),
(lesson_ids[10], chapter_ids[5], course_ids[2], 'Lesson 1.2: The WHERE clause', 'sql-where', 'How to filter data.', 2, NULL, NULL),
(lesson_ids[11], chapter_ids[6], course_ids[2], 'Lesson 2.1: INNER JOIN', 'sql-inner-join', 'Combining two tables.', 1, NULL, NULL),
(lesson_ids[12], chapter_ids[6], course_ids[2], 'Lesson 2.2: LEFT JOIN', 'sql-left-join', 'Keeping all records from the left table.', 2, NULL, NULL),
(lesson_ids[13], chapter_ids[7], course_ids[2], 'Lesson 3.1: COUNT()', 'sql-count', 'Counting rows.', 1, NULL, NULL),
(lesson_ids[14], chapter_ids[7], course_ids[2], 'Lesson 3.2: GROUP BY', 'sql-group-by', 'Aggregating data.', 2, NULL, NULL),
(lesson_ids[15], chapter_ids[8], course_ids[2], 'Lesson 4.1: VARCHAR and TEXT', 'sql-text-types', 'Storing string data.', 1, NULL, NULL),
(lesson_ids[16], chapter_ids[8], course_ids[2], 'Lesson 4.2: INTEGER and DECIMAL', 'sql-numeric-types', 'Storing numeric data.', 2, NULL, NULL),
-- Lessons 17-32: Video only (Original video_url changed to NULL)
(lesson_ids[17], chapter_ids[9], course_ids[3], 'Lesson 1.1: What is a Container?', 'docker-container', NULL, 1, NULL, NULL),
(lesson_ids[18], chapter_ids[9], course_ids[3], 'Lesson 1.2: Dockerfile Basics', 'docker-dockerfile', NULL, 2, NULL, NULL),
(lesson_ids[19], chapter_ids[10], course_ids[3], 'Lesson 2.1: docker-compose.yml', 'docker-compose-file', NULL, 1, NULL, NULL),
(lesson_ids[20], chapter_ids[10], course_ids[3], 'Lesson 2.2: Networking', 'docker-compose-networking', NULL, 2, NULL, NULL),
(lesson_ids[21], chapter_ids[11], course_ids[3], 'Lesson 3.1: Pods', 'k8s-pods', NULL, 1, NULL, NULL),
(lesson_ids[22], chapter_ids[11], course_ids[3], 'Lesson 3.2: Services', 'k8s-services', NULL, 2, NULL, NULL),
(lesson_ids[23], chapter_ids[12], course_ids[3], 'Lesson 4.1: What is Helm?', 'helm-intro', NULL, 1, NULL, NULL),
(lesson_ids[24], chapter_ids[12], course_ids[3], 'Lesson 4.2: Creating a Chart', 'helm-create-chart', NULL, 2, NULL, NULL),
(lesson_ids[25], chapter_ids[13], course_ids[4], 'Lesson 1.1: Functional Components', 'react-functional-components', NULL, 1, NULL, NULL),
(lesson_ids[26], chapter_ids[13], course_ids[4], 'Lesson 1.2: Class Components', 'react-class-components', NULL, 2, NULL, NULL),
(lesson_ids[27], chapter_ids[14], course_ids[4], 'Lesson 2.1: Lifting State Up', 'react-lifting-state', NULL, 1, NULL, NULL),
(lesson_ids[28], chapter_ids[14], course_ids[4], 'Lesson 2.2: Props Drilling', 'react-props-drilling', NULL, 2, NULL, NULL),
(lesson_ids[29], chapter_ids[15], course_ids[4], 'Lesson 3.1: onClick', 'react-onclick', NULL, 1, NULL, NULL),
(lesson_ids[30], chapter_ids[15], course_ids[4], 'Lesson 3.2: onChange', 'react-onchange', NULL, 2, NULL, NULL),
(lesson_ids[31], chapter_ids[16], course_ids[4], 'Lesson 4.1: useState', 'react-usestate', NULL, 1, NULL, NULL),
(lesson_ids[32], chapter_ids[16], course_ids[4], 'Lesson 4.2: useEffect', 'react-useeffect', NULL, 2, NULL, NULL),
-- Lessons 33-40: Quiz only/Other (Original quiz_id changed to NULL)
(lesson_ids[33], chapter_ids[17], course_ids[5], 'Lesson 1.1: Big O Notation', 'big-o-notation', NULL, 1, NULL, NULL),
(lesson_ids[34], chapter_ids[17], course_ids[5], 'Lesson 1.2: Dynamic Arrays', 'dynamic-arrays', NULL, 2, NULL, NULL),
(lesson_ids[35], chapter_ids[18], course_ids[5], 'Lesson 2.1: List Operations', 'linked-list-ops', NULL, 1, NULL, NULL),
(lesson_ids[36], chapter_ids[18], course_ids[5], 'Lesson 2.2: Runner Technique', 'linked-list-runner', NULL, 2, NULL, NULL),
(lesson_ids[37], chapter_ids[19], course_ids[5], 'Lesson 3.1: Traversal', 'tree-traversal', 'Content about traversal.', 1, NULL, NULL),
(lesson_ids[38], chapter_ids[19], course_ids[5], 'Lesson 3.2: Insertion', 'tree-insertion', 'Content about insertion.', 2, NULL, NULL),
(lesson_ids[39], chapter_ids[20], course_ids[5], 'Lesson 4.1: Merge Sort', 'merge-sort', 'Content about merge sort.', 1, NULL, NULL),
(lesson_ids[40], chapter_ids[20], course_ids[5], 'Lesson 4.2: Quick Sort', 'quick-sort', 'Content about quick sort.', 2, NULL, NULL);

    -- 7. SEED ENROLLMENTS (20 records)
    INSERT INTO enrollments (member_id, course_id, member_type, role) VALUES
    (student1_id, course_ids[1], 'STUDENT', 'MEMBER'), (student1_id, course_ids[2], 'STUDENT', 'MEMBER'), (student1_id, course_ids[3], 'STUDENT', 'MEMBER'), (student1_id, course_ids[4], 'STUDENT', 'MEMBER'),
    (student2_id, course_ids[1], 'STUDENT', 'MEMBER'), (student2_id, course_ids[2], 'STUDENT', 'MEMBER'), (student2_id, course_ids[3], 'STUDENT', 'MEMBER'), (student2_id, course_ids[5], 'STUDENT', 'MEMBER'),
    (student3_id, course_ids[1], 'STUDENT', 'MEMBER'), (student3_id, course_ids[4], 'STUDENT', 'MEMBER'), (student3_id, course_ids[5], 'STUDENT', 'MEMBER'),
    (student1_id, course_ids[5], 'STUDENT', 'MEMBER'),
    (student2_id, course_ids[4], 'STUDENT', 'MEMBER'),
    (student3_id, course_ids[2], 'STUDENT', 'MEMBER'), (student3_id, course_ids[3], 'STUDENT', 'MEMBER'),
    (instructor_id, course_ids[1], 'STUDENT', 'MEMBER'),
    (instructor_id, course_ids[2], 'STUDENT', 'MEMBER'),
    (instructor_id, course_ids[3], 'STUDENT', 'MEMBER'),
    (instructor_id, course_ids[4], 'STUDENT', 'MEMBER'),
    (instructor_id, course_ids[5], 'STUDENT', 'MEMBER');

    -- 8. SEED COURSE PROGRESS (20+ records)
    INSERT INTO course_progress (member_id, lesson_id, chapter_id, course_id, status) VALUES
    (student1_id, lesson_ids[1], chapter_ids[1], course_ids[1], 'COMPLETE'), (student1_id, lesson_ids[2], chapter_ids[1], course_ids[1], 'COMPLETE'),
    (student1_id, lesson_ids[3], chapter_ids[2], course_ids[1], 'INCOMPLETE'),
    (student2_id, lesson_ids[1], chapter_ids[1], course_ids[1], 'COMPLETE'), (student2_id, lesson_ids[2], chapter_ids[1], course_ids[1], 'COMPLETE'),
    (student2_id, lesson_ids[3], chapter_ids[2], course_ids[1], 'COMPLETE'), (student2_id, lesson_ids[4], chapter_ids[2], course_ids[1], 'COMPLETE'),
    (student1_id, lesson_ids[9], chapter_ids[5], course_ids[2], 'COMPLETE'), (student1_id, lesson_ids[10], chapter_ids[5], course_ids[2], 'INCOMPLETE'),
    (student2_id, lesson_ids[9], chapter_ids[5], course_ids[2], 'COMPLETE'),
    (student3_id, lesson_ids[1], chapter_ids[1], course_ids[1], 'COMPLETE'), (student3_id, lesson_ids[2], chapter_ids[1], course_ids[1], 'COMPLETE'),
    (student3_id, lesson_ids[13], chapter_ids[7], course_ids[2], 'COMPLETE'), (student3_id, lesson_ids[14], chapter_ids[7], course_ids[2], 'COMPLETE'),
    (student1_id, lesson_ids[17], chapter_ids[9], course_ids[3], 'COMPLETE'), (student1_id, lesson_ids[18], chapter_ids[9], course_ids[3], 'COMPLETE'),
    (student2_id, lesson_ids[17], chapter_ids[9], course_ids[3], 'COMPLETE'),
    (student1_id, lesson_ids[25], chapter_ids[13], course_ids[4], 'COMPLETE'),
    (student2_id, lesson_ids[25], chapter_ids[13], course_ids[4], 'COMPLETE'),
    (student3_id, lesson_ids[25], chapter_ids[13], course_ids[4], 'COMPLETE');

    -- 9. SEED QUIZ QUESTIONS (40 records)
    INSERT INTO quiz_questions (id, quiz_id, question, type, options, correct_answer, marks) VALUES
    (question_ids[1], quiz_ids[1], 'What does IoC stand for?', 'SINGLE_CHOICE', '[{"option": "Inversion of Control"}, {"option": "Injection of Control"}]', 'Inversion of Control', 5),
    (question_ids[2], quiz_ids[1], 'What is a benefit of IoC?', 'SINGLE_CHOICE', '[{"option": "Decoupling"}, {"option": "Tighter Coupling"}]', 'Decoupling', 5),
    (question_ids[3], quiz_ids[2], 'Which annotation marks a method to be called after a bean is constructed?', 'SINGLE_CHOICE', '[{"option": "@PostConstruct"}, {"option": "@PreDestroy"}]', '@PostConstruct', 5),
    (question_ids[4], quiz_ids[2], 'Which annotation marks a method to be called before a bean is destroyed?', 'SINGLE_CHOICE', '[{"option": "@PostConstruct"}, {"option": "@PreDestroy"}]', '@PreDestroy', 5),
    (question_ids[5], quiz_ids[3], 'What is the primary annotation for a REST controller?', 'SINGLE_CHOICE', '[{"option": "@Controller"}, {"option": "@RestController"}]', '@RestController', 5),
    (question_ids[6], quiz_ids[3], 'Which HTTP status code means "Created"?', 'SINGLE_CHOICE', '[{"option": "200"}, {"option": "201"}]', '201', 5),
    (question_ids[7], quiz_ids[4], 'Which annotation maps a GET request?', 'SINGLE_CHOICE', '[{"option": "@GetMapping"}, {"option": "@PostMapping"}]', '@GetMapping', 5),
    (question_ids[8], quiz_ids[4], 'How do you define a path variable?', 'SINGLE_CHOICE', '[{"option": "{id}"}, {"option": "?id="}]', '{id}', 5),
    (question_ids[9], quiz_ids[5], 'What does JPA stand for?', 'SINGLE_CHOICE', '[{"option": "Java Persistence API"}, {"option": "Java Persistent Application"}]', 'Java Persistence API', 5),
    (question_ids[10], quiz_ids[5], 'Which annotation marks a class as an entity?', 'SINGLE_CHOICE', '[{"option": "@Component"}, {"option": "@Entity"}]', '@Entity', 5),
    (question_ids[11], quiz_ids[9], 'Which keyword is used to query data?', 'SINGLE_CHOICE', '[{"option": "GET"}, {"option": "SELECT"}]', 'SELECT', 5),
    (question_ids[12], quiz_ids[9], 'Which keyword specifies the table?', 'SINGLE_CHOICE', '[{"option": "FROM"}, {"option": "TABLE"}]', 'FROM', 5),
    (question_ids[13], quiz_ids[10], 'Which clause filters results?', 'SINGLE_CHOICE', '[{"option": "FILTER"}, {"option": "WHERE"}]', 'WHERE', 5),
    (question_ids[14], quiz_ids[10], 'Which operator means "equal to"?', 'SINGLE_CHOICE', '[{"option": "="}, {"option": "=="}]', '=', 5),
    (question_ids[15], quiz_ids[17], 'What is a lightweight, standalone, executable package?', 'SINGLE_CHOICE', '[{"option": "Image"}, {"option": "Container"}]', 'Container', 5),
    (question_ids[16], quiz_ids[17], 'What is a template for creating containers?', 'SINGLE_CHOICE', '[{"option": "Image"}, {"option": "Container"}]', 'Image', 5),
    (question_ids[17], quiz_ids[18], 'What is the default name of the instruction file for Docker?', 'SINGLE_CHOICE', '[{"option": "Dockerfile"}, {"option": "Docker.file"}]', 'Dockerfile', 5),
    (question_ids[18], quiz_ids[18], 'Which instruction sets the base image?', 'SINGLE_CHOICE', '[{"option": "FROM"}, {"option": "BASE"}]', 'FROM', 5),
    (question_ids[19], quiz_ids[19], 'What is the default name of the compose file?', 'SINGLE_CHOICE', '[{"option": "docker-compose.yml"}, {"option": "compose.yml"}]', 'docker-compose.yml', 5),
    (question_ids[20], quiz_ids[19], 'Which top-level key defines the containers?', 'SINGLE_CHOICE', '[{"option": "containers"}, {"option": "services"}]', 'services', 5);

    -- 10. SEED QUIZ SUBMISSIONS (20+ records)
    -- Note: 'course_id', 'score_out_of', 'passing_percentage' columns are removed.
    INSERT INTO quiz_submissions (quiz_id, member_id, score, percentage, result) VALUES
    (quiz_ids[1], student1_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[1], student2_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[2], student1_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[3], student1_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[9], student1_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[9], student2_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[10], student1_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[17], student1_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[17], student2_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[18], student1_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[4], student2_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[5], student2_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[6], student3_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[7], student3_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[11], student3_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[12], student3_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[13], student3_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[19], student2_id, 10, 100, '{"result": "Pass"}'),
    (quiz_ids[20], student2_id, 5, 50, '{"result": "Fail"}'),
    (quiz_ids[1], student3_id, 10, 100, '{"result": "Pass"}');

END $$;