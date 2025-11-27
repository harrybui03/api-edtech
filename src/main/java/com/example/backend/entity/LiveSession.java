package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "live_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "janus_session_id")
    private Long janusSessionId;
    
    @Column(name = "janus_handle_id")
    private Long janusHandleId;
    
    @Column(name = "room_id")
    private Long roomId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LiveStatus status;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "started_at")
    private OffsetDateTime startedAt;
    
    @Column(name = "ended_at")
    private OffsetDateTime endedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recording_status", length = 20)
    private RecordingStatus recordingStatus;
    
    @Column(name = "final_video_object_name", length = 500)
    private String finalVideoObjectName;
    
    @Column(name = "recording_duration")
    private Integer recordingDuration;
    
    @Column(name = "total_chunks")
    private Integer totalChunks;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    public enum LiveStatus {
        PUBLISHED,
        ENDED
    }
    
    public enum RecordingStatus {
        NOT_STARTED,
        RECORDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}

