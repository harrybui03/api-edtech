
----------------------------------------------------------------------
-- 1. Create Batch Table
----------------------------------------------------------------------
CREATE TABLE Batch (
                      id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       slug VARCHAR(255) UNIQUE NOT NULL,
                       image VARCHAR(255),
                       video_link VARCHAR(255),
                       paid_batch BOOLEAN NOT NULL DEFAULT FALSE,
                       actual_price DECIMAL(10, 2),
                       selling_price DECIMAL(10, 2),
                       amount_usd DECIMAL(10, 2),
                       language VARCHAR(50),
                       open_time TIMESTAMP WITHOUT TIME ZONE,
                       close_time TIMESTAMP WITHOUT TIME ZONE,
                       start_time TIMESTAMP WITHOUT TIME ZONE,
                       end_time TIMESTAMP WITHOUT TIME ZONE,
                       status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
                       max_capacity INTEGER,
                       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

----------------------------------------------------------------------
-- 2. Create Contests Table (Independent, but with optional FK to Batch)
----------------------------------------------------------------------
CREATE TABLE Contests (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          quiz_id UUID,
                          batch_id UUID,
                          title VARCHAR(255) NOT NULL,
                          description TEXT,
                          start_time TIMESTAMP WITHOUT TIME ZONE,
                          end_time TIMESTAMP WITHOUT TIME ZONE,
                          durations INTEGER,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE
);

----------------------------------------------------------------------
-- 3. Create batch_discussion Table (Self-referencing for replies)
----------------------------------------------------------------------
CREATE TABLE batch_discussion (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  batch_id UUID NOT NULL,
                                  user_id UUID NOT NULL, -- Assuming 'users' table exists
                                  title VARCHAR(255) NOT NULL,
                                  content TEXT NOT NULL,
                                  reply_discussion UUID, -- Parent discussion ID
                                  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

----------------------------------------------------------------------
-- 4. Create batch_instructors Table (Mapping)
----------------------------------------------------------------------
CREATE TABLE batch_instructors (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   batch_id UUID NOT NULL,
                                   instructor_id UUID NOT NULL, -- Assuming 'instructors' table exists
                                   UNIQUE (batch_id, instructor_id)
);

----------------------------------------------------------------------
-- 5. Create batch_enrollment Table (Mapping)
----------------------------------------------------------------------
CREATE TABLE batch_enrollment (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  user_id UUID NOT NULL, -- Assuming 'users' table exists
                                  batch_id UUID NOT NULL,
                                  member_type VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
                                  enrolled_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE (user_id, batch_id)
);

----------------------------------------------------------------------
-- 6. Create batch_document Table
----------------------------------------------------------------------
CREATE TABLE batch_document (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                batch_discussion_id UUID NOT NULL,
                                file_url VARCHAR(255) NOT NULL,
                                uploaded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

----------------------------------------------------------------------
-- 7. Define Foreign Key Constraints
----------------------------------------------------------------------

-- Contests FK
ALTER TABLE Contests
    ADD CONSTRAINT fk_contests_batch
        FOREIGN KEY (batch_id) REFERENCES Batch(id) ON DELETE SET NULL;

-- Batch_Instructors FK
ALTER TABLE batch_instructors
    ADD CONSTRAINT fk_batch_instructors_batch
        FOREIGN KEY (batch_id) REFERENCES Batch(id) ON DELETE CASCADE;

-- Batch_Enrollment FK
ALTER TABLE batch_enrollment
    ADD CONSTRAINT fk_batch_enrollment_batch
        FOREIGN KEY (batch_id) REFERENCES Batch(id) ON DELETE CASCADE;

-- Batch_Discussion FK to Batch
ALTER TABLE batch_discussion
    ADD CONSTRAINT fk_batch_discussion_batch
        FOREIGN KEY (batch_id) REFERENCES Batch(id) ON DELETE CASCADE;

-- Batch_Discussion Self-referencing FK
ALTER TABLE batch_discussion
    ADD CONSTRAINT fk_batch_discussion_reply
        FOREIGN KEY (reply_discussion) REFERENCES batch_discussion(id) ON DELETE SET NULL;

-- Batch_Document FK
ALTER TABLE batch_document
    ADD CONSTRAINT fk_batch_document_discussion
        FOREIGN KEY (batch_discussion_id) REFERENCES batch_discussion(id) ON DELETE CASCADE;