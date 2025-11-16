-- Add recording fields to live_sessions table
ALTER TABLE live_sessions 
ADD COLUMN recording_object_name VARCHAR(500),
ADD COLUMN recording_status VARCHAR(20) DEFAULT 'NOT_STARTED',
ADD COLUMN recording_duration INTEGER;

-- Create index for faster recording queries
CREATE INDEX idx_live_sessions_recording_status ON live_sessions(recording_status);
CREATE INDEX idx_live_sessions_room_id ON live_sessions(room_id);

