package com.example.backend.repository;

import com.example.backend.entity.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, UUID> {
    
    Optional<LiveSession> findByRoomId(Long roomId);
    
    List<LiveSession> findByInstructorIdAndStatus(UUID instructorId, LiveSession.LiveStatus status);
    
    List<LiveSession> findByBatchIdAndStatus(UUID batchId, LiveSession.LiveStatus status);
    
    @Query("SELECT ls FROM LiveSession ls WHERE ls.janusSessionId = :sessionId")
    Optional<LiveSession> findByJanusSessionId(@Param("sessionId") Long sessionId);
    
    @Query("SELECT ls FROM LiveSession ls WHERE ls.batch.id = :batchId AND ls.recordingStatus = 'COMPLETED' ORDER BY ls.startedAt DESC")
    List<LiveSession> findCompletedRecordingsByBatchId(@Param("batchId") UUID batchId);
}

