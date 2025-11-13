-- Create live_sessions table
CREATE TABLE live_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    janus_session_id BIGINT,
    janus_handle_id BIGINT,
    room_id BIGINT NOT NULL,
    instructor_id UUID NOT NULL REFERENCES users(id),
    batch_id UUID REFERENCES batch(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    title VARCHAR(255),
    description TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_room_id UNIQUE (room_id)
);

-- Create indexes
CREATE INDEX idx_live_sessions_instructor_id ON live_sessions(instructor_id);
CREATE INDEX idx_live_sessions_batch_id ON live_sessions(batch_id);
CREATE INDEX idx_live_sessions_status ON live_sessions(status);
CREATE INDEX idx_live_sessions_room_id ON live_sessions(room_id);

-- Add comment
COMMENT ON TABLE live_sessions IS 'Stores live streaming session information';

