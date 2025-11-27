-- Add recording fields back to live_sessions for frontend-based recording
ALTER TABLE live_sessions 
ADD COLUMN recording_status VARCHAR(20) DEFAULT 'NOT_STARTED',
ADD COLUMN final_video_object_name VARCHAR(500),
ADD COLUMN recording_duration INTEGER,
ADD COLUMN total_chunks INTEGER DEFAULT 0;
