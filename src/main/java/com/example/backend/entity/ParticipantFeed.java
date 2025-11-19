package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Tracks individual feeds (streams) from participants in live sessions
 * Each participant can have multiple feeds (camera, screen share, etc.)
 */
@Entity
@Table(name = "participant_feeds", indexes = {
    @Index(name = "idx_participant_feeds_room_user", columnList = "room_id,user_id"),
    @Index(name = "idx_participant_feeds_room_feed", columnList = "room_id,feed_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantFeed {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * The live session room ID
     */
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    /**
     * The user who owns this feed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Janus feed ID (same as handle ID when published)
     * This is what other participants subscribe to
     */
    @Column(name = "feed_id", nullable = false)
    private Long feedId;
    
    /**
     * Type of feed (camera, screen, audio-only, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "feed_type", nullable = false)
    private FeedType feedType;
    
    /**
     * Janus session ID for this feed
     */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;
    
    /**
     * Janus handle ID for this feed
     */
    @Column(name = "handle_id", nullable = false)
    private Long handleId;
    
    /**
     * Display name shown in the room
     */
    @Column(name = "display_name", length = 255)
    private String displayName;
    
    /**
     * Whether this feed is currently active/published
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "ended_at")
    private OffsetDateTime endedAt;
    
    public enum FeedType {
        CAMERA,        // Camera + mic stream
        SCREEN,        // Screen share
        AUDIO_ONLY,    // Audio only
        SCREEN_AUDIO   // Screen with audio
    }
}

