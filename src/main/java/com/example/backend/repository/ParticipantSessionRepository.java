package com.example.backend.repository;

import com.example.backend.entity.ParticipantSession;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantSessionRepository extends JpaRepository<ParticipantSession, UUID> {
    
    /**
     * Find active session for a user in a specific room
     */
    Optional<ParticipantSession> findByUserAndRoomIdAndIsActiveTrue(User user, Long roomId);
    
    /**
     * Find all active sessions in a room
     */
    List<ParticipantSession> findByRoomIdAndIsActiveTrue(Long roomId);
    
    /**
     * Find by Janus session ID
     */
    Optional<ParticipantSession> findByJanusSessionId(Long janusSessionId);
    
    /**
     * Deactivate a specific session
     */
    @Modifying
    @Query("UPDATE ParticipantSession ps SET ps.isActive = false, ps.endedAt = CURRENT_TIMESTAMP WHERE ps.id = :id")
    int deactivateSession(UUID id);
    
    /**
     * Deactivate session by Janus session ID
     */
    @Modifying
    @Query("UPDATE ParticipantSession ps SET ps.isActive = false, ps.endedAt = CURRENT_TIMESTAMP WHERE ps.janusSessionId = :janusSessionId")
    int deactivateByJanusSessionId(Long janusSessionId);
    
    /**
     * Deactivate all sessions in a room (when live ends)
     */
    @Modifying
    @Query("UPDATE ParticipantSession ps SET ps.isActive = false, ps.endedAt = CURRENT_TIMESTAMP WHERE ps.roomId = :roomId AND ps.isActive = true")
    int deactivateAllSessionsInRoom(Long roomId);
    
    /**
     * Deactivate user's session in a room (when kicked)
     */
    @Modifying
    @Query("UPDATE ParticipantSession ps SET ps.isActive = false, ps.endedAt = CURRENT_TIMESTAMP WHERE ps.user.id = :userId AND ps.roomId = :roomId AND ps.isActive = true")
    int deactivateUserSessionInRoom(UUID userId, Long roomId);
}

