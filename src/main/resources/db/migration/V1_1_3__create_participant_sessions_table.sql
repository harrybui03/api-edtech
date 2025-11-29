-- Create participant_sessions table to track main Janus session for each user in a room
CREATE TABLE participant_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    janus_session_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ,
    
    CONSTRAINT fk_participant_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_participant_sessions_room ON participant_sessions(room_id);
CREATE INDEX idx_participant_sessions_session ON participant_sessions(janus_session_id);

-- Add comments
COMMENT ON TABLE participant_sessions IS 'Tracks the main Janus session for each participant in a room. Each user has ONE session per room with multiple handles for different feeds.';
COMMENT ON COLUMN participant_sessions.janus_session_id IS 'The main Janus session ID - all handles (camera, screen) are attached to this session';

