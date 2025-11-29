-- Add display_name column to participant_sessions to store join display name
ALTER TABLE participant_sessions
ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);


