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
 * Tracks the main Janus session for each participant in a room
 * Each user has ONE session per room, with multiple handles for different feeds (camera, screen)
 */
@Entity
@Table(name = "participant_sessions", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_participant_room", columnNames = {"user_id", "room_id"})
       },
       indexes = {
           @Index(name = "idx_participant_sessions_room", columnList = "room_id"),
           @Index(name = "idx_participant_sessions_session", columnList = "janus_session_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * The live session room ID
     */
    @Column(name = "room_id", nullable = false)
    private Long roomId;
    
    /**
     * The user who owns this session
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * The display name that user used when joining the room.
     * This is stored separately to support kicking by join name.
     */
    @Column(name = "display_name")
    private String displayName;
    
    /**
     * Janus session ID - this is the main session for this user in this room
     * All handles (camera, screen) are attached to this session
     */
    @Column(name = "janus_session_id", nullable = false)
    private Long janusSessionId;
    
    /**
     * Whether this session is currently active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "ended_at")
    private OffsetDateTime endedAt;
}

