-- Add user_id column to jobs table to track the job creator
ALTER TABLE jobs ADD COLUMN user_id UUID;

-- Add foreign key constraint to link to the users table
ALTER TABLE jobs
    ADD CONSTRAINT fk_jobs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
