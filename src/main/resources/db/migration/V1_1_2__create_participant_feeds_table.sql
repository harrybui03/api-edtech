-- Create participant_feeds table to track individual feeds/streams from participants
-- Each participant can have multiple feeds (camera, screen share, etc.)

CREATE TABLE participant_feeds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id BIGINT NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    feed_id BIGINT NOT NULL,
    feed_type VARCHAR(20) NOT NULL CHECK (feed_type IN ('CAMERA', 'SCREEN', 'AUDIO_ONLY', 'SCREEN_AUDIO')),
    session_id BIGINT NOT NULL,
    handle_id BIGINT NOT NULL,
    display_name VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ
);

-- Indexes for efficient queries
CREATE INDEX idx_participant_feeds_room_user ON participant_feeds(room_id, user_id);
CREATE INDEX idx_participant_feeds_room_feed ON participant_feeds(room_id, feed_id);
CREATE INDEX idx_participant_feeds_session_handle ON participant_feeds(session_id, handle_id);
CREATE INDEX idx_participant_feeds_active ON participant_feeds(room_id, is_active) WHERE is_active = true;

-- Comments
COMMENT ON TABLE participant_feeds IS 'Tracks individual feeds (streams) from participants in live sessions';
COMMENT ON COLUMN participant_feeds.feed_id IS 'Janus feed ID (same as handle ID when published) - what other participants subscribe to';
COMMENT ON COLUMN participant_feeds.feed_type IS 'Type of feed: CAMERA (camera+mic), SCREEN (screen share), AUDIO_ONLY, SCREEN_AUDIO';
COMMENT ON COLUMN participant_feeds.is_active IS 'Whether this feed is currently active/published';

