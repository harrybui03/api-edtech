package com.example.backend.repository;

import com.example.backend.entity.ParticipantFeed;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantFeedRepository extends JpaRepository<ParticipantFeed, UUID> {
    
    /**
     * Find all active feeds for a user in a specific room
     */
    List<ParticipantFeed> findByUserAndRoomIdAndIsActiveTrue(User user, Long roomId);
    
    /**
     * Find all active feeds in a room
     */
    List<ParticipantFeed> findByRoomIdAndIsActiveTrue(Long roomId);
    
    /**
     * Find a specific feed by session and handle
     */
    Optional<ParticipantFeed> findBySessionIdAndHandleId(Long sessionId, Long handleId);
    
    /**
     * Find feed by feed ID and room ID
     */
    Optional<ParticipantFeed> findByFeedIdAndRoomId(Long feedId, Long roomId);
    
    /**
     * Get all feed IDs for a user in a room (for filtering)
     */
    @Query("SELECT pf.feedId FROM ParticipantFeed pf WHERE pf.user.id = :userId AND pf.roomId = :roomId AND pf.isActive = true")
    List<Long> findActiveFeedIdsByUserAndRoom(UUID userId, Long roomId);
    
    /**
     * Deactivate all feeds for a user in a room
     */
    @Modifying
    @Query("UPDATE ParticipantFeed pf SET pf.isActive = false, pf.endedAt = CURRENT_TIMESTAMP WHERE pf.user.id = :userId AND pf.roomId = :roomId AND pf.isActive = true")
    int deactivateUserFeedsInRoom(UUID userId, Long roomId);
    
    /**
     * Deactivate all feeds in a room (when session ends)
     */
    @Modifying
    @Query("UPDATE ParticipantFeed pf SET pf.isActive = false, pf.endedAt = CURRENT_TIMESTAMP WHERE pf.roomId = :roomId AND pf.isActive = true")
    int deactivateAllFeedsInRoom(Long roomId);
    
    /**
     * Deactivate a specific feed
     */
    @Modifying
    @Query("UPDATE ParticipantFeed pf SET pf.isActive = false, pf.endedAt = CURRENT_TIMESTAMP WHERE pf.sessionId = :sessionId AND pf.handleId = :handleId")
    int deactivateFeed(Long sessionId, Long handleId);
    
    /**
     * Check if user has active camera feed in room
     */
    @Query("SELECT pf FROM ParticipantFeed pf WHERE pf.user.id = :userId AND pf.roomId = :roomId AND pf.feedType = 'CAMERA' AND pf.isActive = true")
    Optional<ParticipantFeed> findActiveCameraFeed(UUID userId, Long roomId);
    
    /**
     * Check if user has active screen feed in room
     */
    @Query("SELECT pf FROM ParticipantFeed pf WHERE pf.user.id = :userId AND pf.roomId = :roomId AND pf.feedType = 'SCREEN' AND pf.isActive = true")
    Optional<ParticipantFeed> findActiveScreenFeed(UUID userId, Long roomId);
}

