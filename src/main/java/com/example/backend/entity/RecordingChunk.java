package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recording_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingChunk {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;
    
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;
    
    @Column(name = "object_name", nullable = false, length = 500)
    private String objectName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChunkStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
    
    public enum ChunkStatus {
        UPLOADED,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}

