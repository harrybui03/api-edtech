package com.example.backend.repository;

import com.example.backend.entity.RecordingChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecordingChunkRepository extends JpaRepository<RecordingChunk, UUID> {
    
    @Query("SELECT rc FROM RecordingChunk rc WHERE rc.liveSession.id = :liveSessionId ORDER BY rc.chunkIndex ASC")
    List<RecordingChunk> findByLiveSessionIdOrderByChunkIndex(UUID liveSessionId);
    
    @Query("SELECT COUNT(rc) FROM RecordingChunk rc WHERE rc.liveSession.id = :liveSessionId")
    Integer countByLiveSessionId(UUID liveSessionId);
    
    @Query("SELECT rc FROM RecordingChunk rc WHERE rc.liveSession.id = :liveSessionId AND rc.chunkIndex = :chunkIndex")
    RecordingChunk findByLiveSessionIdAndChunkIndex(UUID liveSessionId, Integer chunkIndex);
}

