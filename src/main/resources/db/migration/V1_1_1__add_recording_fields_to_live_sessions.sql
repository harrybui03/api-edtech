-- Add recording fields to live_sessions table
ALTER TABLE live_sessions 
ADD COLUMN recording_object_name VARCHAR(500),
ADD COLUMN recording_status VARCHAR(20) DEFAULT 'NOT_STARTED',
ADD COLUMN recording_duration INTEGER;
