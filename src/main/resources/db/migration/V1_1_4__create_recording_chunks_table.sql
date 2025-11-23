-- Create recording_chunks table to store metadata for each uploaded recording chunk
CREATE TABLE recording_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    live_session_id UUID NOT NULL REFERENCES live_sessions(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    object_name VARCHAR(500) NOT NULL,
    file_size BIGINT,
    duration_seconds INTEGER,
    status VARCHAR(20) NOT NULL CHECK (status IN ('UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_recording_chunk UNIQUE (live_session_id, chunk_index)
);

-- Create indexes
CREATE INDEX idx_recording_chunks_session ON recording_chunks(live_session_id);
CREATE INDEX idx_recording_chunks_status ON recording_chunks(live_session_id, status);

-- Add comments
COMMENT ON TABLE recording_chunks IS 'Stores metadata for each recording chunk uploaded from frontend. Each chunk is ~30 seconds of recorded video.';
COMMENT ON COLUMN recording_chunks.chunk_index IS 'Sequential index of chunk (0, 1, 2, ...) to maintain order for merging';
COMMENT ON COLUMN recording_chunks.object_name IS 'MinIO object path where the chunk is stored';

