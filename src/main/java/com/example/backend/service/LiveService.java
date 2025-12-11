package com.example.backend.service;

import com.example.backend.dto.request.live.*;
import com.example.backend.dto.response.live.*;
import com.example.backend.entity.Batch;
import com.example.backend.entity.LiveSession;
import com.example.backend.entity.ParticipantFeed;
import com.example.backend.entity.ParticipantSession;
import com.example.backend.entity.User;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.mapper.LiveSessionMapper;
import com.example.backend.repository.BatchEnrollmentRepository;
import com.example.backend.repository.BatchRepository;
import com.example.backend.repository.LiveSessionRepository;
import com.example.backend.repository.ParticipantFeedRepository;
import com.example.backend.repository.ParticipantSessionRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveService {
    
    private final JanusService janusService;
    private final LiveSessionRepository liveSessionRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final LiveSessionMapper liveSessionMapper;
    private final ParticipantFeedRepository participantFeedRepository;
    private final ParticipantSessionRepository participantSessionRepository;
    private final BatchEnrollmentRepository batchEnrollmentRepository;
    
    private final Random random = new Random();

    // Keepalive timers for Janus sessions started by backend (publisher main session)
    private final Map<Long, Timer> sessionKeepaliveTimers = new ConcurrentHashMap<>();
    
    /**
     * Bắt đầu live streaming
     */
    @Transactional
    public LiveSessionResponse startLive(StartLiveRequest request) {
        // Get current user (instructor)
        User instructor = getCurrentUser();
        
        // Validate batch
        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new DataNotFoundException("Batch not found with id: " + request.getBatchId()));
        
        // Check if user is instructor of this batch
        boolean isInstructor = batch.getInstructors().stream()
                .anyMatch(bi -> bi.getInstructor().getId().equals(instructor.getId()));
        
        if (!isInstructor) {
            throw new ForbiddenException("You are not an instructor of this batch");
        }
        
        // Check if there's already a published session for this batch
        liveSessionRepository.findByBatchIdAndStatus(batch.getId(), LiveSession.LiveStatus.PUBLISHED)
                .stream()
                .findFirst()
                .ifPresent(session -> {
                    throw new IllegalStateException("There is already a published live session for this batch");
                });
        
        // Step 1: Create Janus session
        JanusResponse sessionResponse = janusService.createSession();
        Long sessionId = sessionResponse.getData() != null 
                ? ((Number) sessionResponse.getData().get("id")).longValue() 
                : null;
        
        if (sessionId == null) {
            throw new RuntimeException("Failed to create Janus session");
        }
        
        // Step 2: Attach videoroom plugin
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long handleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (handleId == null) {
            throw new RuntimeException("Failed to attach plugin");
        }
        
        // Step 3: Create room with random ID
        Long roomId = generateRoomId();
        janusService.createRoom(sessionId, handleId, roomId);
        
        // Step 4: Save live session to database
        LiveSession liveSession = LiveSession.builder()
                .janusSessionId(sessionId)
                .janusHandleId(handleId)
                .roomId(roomId)
                .instructor(instructor)
                .batch(batch)
                .status(LiveSession.LiveStatus.PUBLISHED)
                .title(request.getTitle())
                .description(request.getDescription())
                .startedAt(OffsetDateTime.now())
                .build();
        
        liveSession = liveSessionRepository.save(liveSession);

        // Start keepalive for the backend-managed publisher session
        startKeepalive(sessionId);
        
        return liveSessionMapper.toResponse(liveSession);
    }
    
    /**
     * Join live streaming - creates ONE main session per user in room
     * This session will be reused for all feeds (camera, screen)
     * User joins as publisher (can publish or just watch others)
     */
    @Transactional
    public JanusResponse joinLive(JoinLiveRequest request) {
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // Check if user already has an active session in this room
        Optional<ParticipantSession> existingSession = participantSessionRepository
                .findByUserAndRoomIdAndIsActiveTrue(currentUser, request.getRoomId());
        
        if (existingSession.isPresent()) {
            JanusResponse response = new JanusResponse();
            response.setSessionId(existingSession.get().getJanusSessionId());
            response.setJanus("success");
            response.setLiveSessionId(liveSession.getId());
            return response;
        }
        
        // Create ONE main session for this user in this room
        JanusResponse sessionResponse = janusService.createSession();
        Long sessionId = sessionResponse.getData() != null 
                ? ((Number) sessionResponse.getData().get("id")).longValue() 
                : null;
        
        if (sessionId == null) {
            throw new RuntimeException("Failed to create Janus session");
        }
        
        // Attach plugin to get a handle for joining room
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long handleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (handleId == null) {
            throw new RuntimeException("Failed to attach plugin");
        }
        
        // Join room as publisher (all users join as publisher)
        String displayName = (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty())
                ? request.getDisplayName().trim()
                : currentUser.getFullName();
        
        String ptype = (request.getPtype() != null && !request.getPtype().trim().isEmpty())
                ? request.getPtype()
                : "publisher";  // Default to publisher
        
        JanusResponse joinResponse = janusService.joinRoom(
                sessionId, 
                handleId, 
                request.getRoomId(), 
                ptype,
                displayName
        );
        
        // Save participant session
        ParticipantSession participantSession = ParticipantSession.builder()
                .roomId(request.getRoomId())
                .user(currentUser)
                .displayName(displayName)
                .janusSessionId(sessionId)
                .isActive(true)
                .build();
        participantSessionRepository.save(participantSession);
        
        // Start keepalive for this main session
        startKeepalive(sessionId);
        
        // Return full response with sessionId and handleId
        joinResponse.setSessionId(sessionId);
        joinResponse.setHandleId(handleId);
        joinResponse.setLiveSessionId(liveSession.getId());
        
        return joinResponse;
    }
    
    /**
     * Publish stream (camera/microphone)
     * REUSES user's main session, creates new handle for camera
     */
    @Transactional
    public PublishStreamResponse publishStream(PublishStreamRequest request) {
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // Check if user already has an active camera feed (use direct query for fresh data)
        Optional<ParticipantFeed> existingCameraFeed = participantFeedRepository
                .findActiveCameraFeed(currentUser.getId(), request.getRoomId());
        
        if (existingCameraFeed.isPresent()) {
            throw new IllegalStateException("User already has an active camera stream in this room. Please unpublish first.");
        }
        
        // Get or create user's main session
        ParticipantSession participantSession = participantSessionRepository
                .findByUserAndRoomIdAndIsActiveTrue(currentUser, request.getRoomId())
                .orElseGet(() -> {
                    // Auto-create session if not exists
                    JanusResponse sessionResponse = janusService.createSession();
                    Long newSessionId = sessionResponse.getData() != null 
                            ? ((Number) sessionResponse.getData().get("id")).longValue() 
                            : null;
                    
                    if (newSessionId == null) {
                        throw new RuntimeException("Failed to create Janus session");
                    }
                    
                    ParticipantSession newSession = ParticipantSession.builder()
                            .roomId(request.getRoomId())
                            .user(currentUser)
                            .displayName(currentUser.getFullName())
                            .janusSessionId(newSessionId)
                            .isActive(true)
                            .build();
                    
                    participantSessionRepository.save(newSession);
                    startKeepalive(newSessionId);
                    
                    return newSession;
                });
        
        Long sessionId = participantSession.getJanusSessionId();
        
        // Create NEW handle for camera on existing session
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long cameraHandleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (cameraHandleId == null) {
            throw new RuntimeException("Failed to attach plugin for camera");
        }
        
        // Join room with camera handle
        String displayName = currentUser.getFullName();
        
        // Get existing feedIds BEFORE join to accurately detect the new one
        java.util.Set<Long> existingFeedIds = getCurrentFeedIds(request.getRoomId());
        
        janusService.joinRoom(sessionId, cameraHandleId, request.getRoomId(), "publisher", displayName);
        
        Long cameraFeedId = pollForNewFeedId(request.getRoomId(), displayName, existingFeedIds, 5000);
        
        if (cameraFeedId == null) {
            // Cleanup: detach handle if we can't get feedId
            try {
                janusService.detachPlugin(sessionId, cameraHandleId);
            } catch (Exception e) {
                // Ignore cleanup error
            }
            throw new RuntimeException("Failed to get feedId from Janus. The publisher may not have been added to the room.");
        }
        
        // Publish stream
        JanusResponse janusResponse = janusService.publishStream(
                sessionId, 
                cameraHandleId, 
                request.getSdp()
        );
        
        // Extract SDP answer from jsep
        String sdpAnswer = null;
        String type = null;
        if (janusResponse.getJsep() != null) {
            type = (String) janusResponse.getJsep().get("type");
            sdpAnswer = (String) janusResponse.getJsep().get("sdp");
        }
        
        // If publish successful, save camera feed to database
        if (sdpAnswer != null && janusResponse.getError() == null) {
            // Use feedId from join response (not from publish response!)
            Long actualFeedId = cameraFeedId;
            
            // Save camera feed to database (active immediately after publish)
            ParticipantFeed cameraFeed = ParticipantFeed.builder()
                    .roomId(request.getRoomId())
                    .user(currentUser)
                    .feedId(actualFeedId)
                    .feedType(ParticipantFeed.FeedType.CAMERA)
                    .sessionId(sessionId)
                    .handleId(cameraHandleId)
                    .displayName(displayName)
                    .isActive(true)
                    .build();
            
            participantFeedRepository.save(cameraFeed);
        } else {
            // Cleanup: Publish failed, detach handle to avoid orphaned publisher in Janus
            try {
                janusService.detachPlugin(sessionId, cameraHandleId);
            } catch (Exception e) {
                // Ignore cleanup error
            }
        }
        
        return PublishStreamResponse.builder()
                .sdpAnswer(sdpAnswer)
                .type(type)
                .sessionId(sessionId)
                .handleId(cameraHandleId)
                .error(janusResponse.getError())
                .errorCode(janusResponse.getErrorCode())
                .build();
    }
    
    /**
     * Publish screen share stream
     * REUSES user's main session, creates new handle for screen
     */
    @Transactional
    public PublishStreamResponse publishScreenShare(PublishStreamRequest request) {
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // Check if user already has an active screen feed (use direct query for fresh data)
        Optional<ParticipantFeed> existingScreenFeed = participantFeedRepository
                .findActiveScreenFeed(currentUser.getId(), request.getRoomId());
        
        if (existingScreenFeed.isPresent()) {
            throw new IllegalStateException("User already has an active screen share in this room. Please stop sharing first.");
        }
        
        // Get or create user's main session
        ParticipantSession participantSession = participantSessionRepository
                .findByUserAndRoomIdAndIsActiveTrue(currentUser, request.getRoomId())
                .orElseGet(() -> {
                    // Auto-create session if not exists
                    JanusResponse sessionResponse = janusService.createSession();
                    Long newSessionId = sessionResponse.getData() != null 
                            ? ((Number) sessionResponse.getData().get("id")).longValue() 
                            : null;
                    
                    if (newSessionId == null) {
                        throw new RuntimeException("Failed to create Janus session");
                    }
                    
                    ParticipantSession newSession = ParticipantSession.builder()
                            .roomId(request.getRoomId())
                            .user(currentUser)
                            .displayName(currentUser.getFullName())
                            .janusSessionId(newSessionId)
                            .isActive(true)
                            .build();
                    
                    participantSessionRepository.save(newSession);
                    startKeepalive(newSessionId);
                    
                    return newSession;
                });
        
        Long sessionId = participantSession.getJanusSessionId();
        
        // Create NEW handle for screen on existing session
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long screenHandleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (screenHandleId == null) {
            throw new RuntimeException("Failed to attach plugin for screen share");
        }
        
        // Join room with screen handle
        String displayName = currentUser.getFullName() + " (Screen)";
        
        // Get existing feedIds BEFORE join to accurately detect the new one
        java.util.Set<Long> existingFeedIds = getCurrentFeedIds(request.getRoomId());
        
        janusService.joinRoom(sessionId, screenHandleId, request.getRoomId(), "publisher", displayName);
        
        // Poll for NEW feedId (not in existingFeedIds) to avoid matching wrong user
        Long screenFeedId = pollForNewFeedId(request.getRoomId(), displayName, existingFeedIds, 5000);
        
        if (screenFeedId == null) {
            // Cleanup: detach handle if we can't get feedId
            try {
                janusService.detachPlugin(sessionId, screenHandleId);
            } catch (Exception e) {
                // Ignore cleanup error
            }
            throw new RuntimeException("Failed to get feedId from Janus. The publisher may not have been added to the room.");
        }
        
        // Publish screen stream
        JanusResponse janusResponse = janusService.publishStream(
                sessionId, 
                screenHandleId, 
                request.getSdp()
        );
        
        // Extract SDP answer from jsep
        String sdpAnswer = null;
        String type = null;
        if (janusResponse.getJsep() != null) {
            type = (String) janusResponse.getJsep().get("type");
            sdpAnswer = (String) janusResponse.getJsep().get("sdp");
        }
        
        // If publish successful, save screen feed to database
        if (sdpAnswer != null && janusResponse.getError() == null) {
            // Use feedId from join response (not from publish response!)
            Long actualFeedId = screenFeedId;
            
            // Save screen share feed to database (active after successful publish)
            ParticipantFeed screenFeed = ParticipantFeed.builder()
                    .roomId(request.getRoomId())
                    .user(currentUser)
                    .feedId(actualFeedId)
                    .feedType(ParticipantFeed.FeedType.SCREEN)
                    .sessionId(sessionId)
                    .handleId(screenHandleId)
                    .displayName(displayName)
                    .isActive(true)
                    .build();
            participantFeedRepository.save(screenFeed);
        } else {
            // Publish failed, detach handle to avoid orphaned publisher in Janus
            try {
                janusService.detachPlugin(sessionId, screenHandleId);
            } catch (Exception e) {
                // Ignore cleanup error
            }
        }
        
        return PublishStreamResponse.builder()
                .sdpAnswer(sdpAnswer)
                .type(type)
                .sessionId(sessionId)
                .handleId(screenHandleId)
                .error(janusResponse.getError())
                .errorCode(janusResponse.getErrorCode())
                .build();
    }
    
    /**
     * Unpublish stream (camera/microphone)
     * Destroys ONLY the specified handle, keeps main session alive
     * Frontend provides sessionId and handleId directly
     */
    @Transactional
    public JanusResponse unpublishStream(UnpublishRequest request) {
        // Validate live session exists
        liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        Long sessionId = request.getSessionId();
        Long handleId = request.getHandleId();
        
        // Deactivate feed in database FIRST
        int updated = participantFeedRepository.deactivateFeed(sessionId, handleId);
        
        // Try to unpublish from Janus (may fail if already unpublished due to ICE timeout, etc.)
        JanusResponse unpublishResponse = null;
        try {
            unpublishResponse = janusService.unpublishStream(sessionId, handleId);
        } catch (Exception e) {
            // Log but continue - handle may already be unpublished
            unpublishResponse = new JanusResponse();
            unpublishResponse.setJanus("error");
            unpublishResponse.setError("Unpublish failed: " + e.getMessage());
        }
        
        // Always detach handle to cleanup resources (even if unpublish failed)
        try {
            janusService.detachPlugin(sessionId, handleId);
        } catch (Exception e) {
            // Log but don't fail - handle may already be detached
        }
        
        // Return success if we updated DB, even if Janus had issues
        if (updated > 0) {
            JanusResponse successResponse = new JanusResponse();
            successResponse.setJanus("success");
            return successResponse;
        }
        
        return unpublishResponse != null ? unpublishResponse : new JanusResponse();
    }
    
    /**
     * Unpublish screen share stream
     * Destroys ONLY the specified handle, keeps main session alive
     * Frontend provides sessionId and handleId directly
     */
    @Transactional
    public JanusResponse unpublishScreenShare(UnpublishRequest request) {
        // Validate live session exists
        liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        Long sessionId = request.getSessionId();
        Long handleId = request.getHandleId();
        
        // Deactivate screen feed in database FIRST
        int updated = participantFeedRepository.deactivateFeed(sessionId, handleId);
        
        // Try to unpublish from Janus (may fail if already unpublished)
        JanusResponse unpublishResponse = null;
        try {
            unpublishResponse = janusService.unpublishStream(sessionId, handleId);
        } catch (Exception e) {
            // Log but continue - handle may already be unpublished
            unpublishResponse = new JanusResponse();
            unpublishResponse.setJanus("error");
            unpublishResponse.setError("Unpublish failed: " + e.getMessage());
        }
        
        // Always detach handle to cleanup resources
        try {
            janusService.detachPlugin(sessionId, handleId);
        } catch (Exception e) {
            // Log but don't fail - handle may already be detached
        }
        
        // Return success if we updated DB, even if Janus had issues
        if (updated > 0) {
            JanusResponse successResponse = new JanusResponse();
            successResponse.setJanus("success");
            return successResponse;
        }
        
        return unpublishResponse != null ? unpublishResponse : new JanusResponse();
    }
    
    /**
     * Kick participant - destroys their main session which disconnects ALL their feeds (camera, screen)
     */
    @Transactional
    public JanusResponse kickParticipant(KickParticipantRequest request) {
        User instructor = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        // Check if user is the instructor who started this session
        if (!liveSession.getInstructor().getId().equals(instructor.getId())) {
            throw new ForbiddenException("Only the instructor who started the session can kick participants");
        }
        
        // Find the user being kicked by their feed ID
        // The participantId in the request is actually a feed ID from Janus
        ParticipantFeed kickedFeed = participantFeedRepository.findByFeedIdAndRoomId(
                request.getParticipantId(), 
                request.getRoomId()
        ).orElseThrow(() -> new DataNotFoundException(
                "Feed not found with ID: " + request.getParticipantId() + " in room " + request.getRoomId()));
        
        User kickedUser = kickedFeed.getUser();
        
        // Find and destroy user's main session
        ParticipantSession userSession = participantSessionRepository
                .findByUserAndRoomIdAndIsActiveTrue(kickedUser, request.getRoomId())
                .orElse(null);
        
        if (userSession != null) {
            Long sessionId = userSession.getJanusSessionId();
            
            // Stop keepalive first
            stopKeepalive(sessionId);
            
            // Destroy the Janus session - this will disconnect ALL handles (camera, screen, etc.)
            try {
                janusService.destroySession(sessionId);
            } catch (Exception e) {
                // Session destruction failed
            }
            
            // Deactivate session in database
            participantSessionRepository.deactivateByJanusSessionId(sessionId);
            
            // Deactivate ALL feeds for this user in this room
            participantFeedRepository.deactivateUserFeedsInRoom(
                    kickedUser.getId(), 
                    request.getRoomId()
            );
        }
        
        // Also call Janus kick API (may not be needed since we destroyed session, but for safety)
        JanusResponse kickResponse = janusService.kickParticipant(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                request.getRoomId(),
                request.getParticipantId()
        );
        
        return kickResponse;
    }
    
    /**
     * List participants (publishers) in room
     * Subscriber dùng để lấy danh sách publishers và feed IDs
     * Automatically excludes current user's own feeds
     */
    public ParticipantListResponse listParticipants(Long roomId, Long excludeFeedId) {
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        ParticipantListResponse response = janusService.listParticipants(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                roomId
        );
        
        // Get all active feed IDs for current user in this room
        List<Long> myFeedIds = participantFeedRepository.findActiveFeedIdsByUserAndRoom(
                currentUser.getId(), 
                roomId
        );
        
        // Add manual excludeFeedId if provided (for backward compatibility)
        if (excludeFeedId != null && !myFeedIds.contains(excludeFeedId)) {
            myFeedIds.add(excludeFeedId);
        }
        
        // Filter out ALL feeds from current user
        if (!myFeedIds.isEmpty() && response.getParticipants() != null) {
            List<ParticipantListResponse.Participant> filteredParticipants = response.getParticipants().stream()
                    .filter(p -> !myFeedIds.contains(p.getId()))
                    .toList();
            response.setParticipants(filteredParticipants);
        }
        
        return response;
    }
    
    /**
     * Subscribe to a publisher's stream
     * REUSES user's main session, creates new handle for subscribing to this feed
     * 
     * Note: Mỗi feed cần 1 handle riêng theo Janus VideoRoom design
     */
    @Transactional
    public SubscribeResponse subscribe(SubscribeRequest request) {
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        // Get or create user's main session (reuse architecture!)
        ParticipantSession participantSession = participantSessionRepository
                .findByUserAndRoomIdAndIsActiveTrue(currentUser, request.getRoomId())
                .orElseGet(() -> {
                    // Auto-create session if not exists (user didn't join first)
                    JanusResponse sessionResponse = janusService.createSession();
                    Long newSessionId = sessionResponse.getData() != null 
                            ? ((Number) sessionResponse.getData().get("id")).longValue() 
                            : null;
                    
                    if (newSessionId == null) {
                        throw new RuntimeException("Failed to create Janus session");
                    }
                    
                    ParticipantSession newSession = ParticipantSession.builder()
                            .roomId(request.getRoomId())
                            .user(currentUser)
                            .janusSessionId(newSessionId)
                            .isActive(true)
                            .build();
                    
                    participantSessionRepository.save(newSession);
                    startKeepalive(newSessionId);
                    
                    return newSession;
                });
        
        Long sessionId = participantSession.getJanusSessionId();
        
        // Create new handle for subscribing to this feed (on existing session)
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long handleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (handleId == null) {
            throw new RuntimeException("Failed to attach plugin for subscriber");
        }
        
        // Join as subscriber with feed - Janus sẽ trả về SDP offer
        JanusResponse configureResponse = janusService.configureSubscriber(
                sessionId,
                handleId,
                request.getRoomId(),
                request.getFeedId()
        );
        
        // Extract SDP offer from jsep
        String sdpOffer = null;
        String type = null;
        if (configureResponse.getJsep() != null) {
            type = (String) configureResponse.getJsep().get("type");
            sdpOffer = (String) configureResponse.getJsep().get("sdp");
        }
        
        return SubscribeResponse.builder()
                .sdpOffer(sdpOffer)
                .type(type)
                .sessionId(sessionId)  // Return main session (same for all!)
                .handleId(handleId)    // Return new handle for this feed
                .feedId(request.getFeedId())
                .error(configureResponse.getError())
                .errorCode(configureResponse.getErrorCode())
                .build();
    }
    
    /**
     * Start subscriber after creating SDP answer
     * Step 2: Gửi SDP answer để bắt đầu nhận stream
     */
    public JanusResponse startSubscriber(StartSubscriberRequest request) {
        return janusService.startSubscriber(request.getSessionId(), request.getHandleId(), request.getSdpAnswer());
    }

    /**
     * Send keepalive for a given Janus session
     */
    public JanusResponse keepAlive(Long sessionId) {
        return janusService.keepAlive(sessionId);
    }
    
    /**
     * Leave room (for participants to clean up their session)
     * Destroys main session which disconnects all feeds
     */
    @Transactional
    public void leaveRoom(Long roomId) {
        User currentUser = getCurrentUser();
        
        // Find user's main session in this room
        ParticipantSession userSession = participantSessionRepository
                .findByUserAndRoomIdAndIsActiveTrue(currentUser, roomId)
                .orElse(null);
        
        if (userSession == null) {
            return;
        }
        
        Long sessionId = userSession.getJanusSessionId();
        
        // Stop keepalive for this session
        stopKeepalive(sessionId);
        
        // Destroy session in Janus - this disconnects ALL handles (camera, screen)
        try {
            janusService.destroySession(sessionId);
        } catch (Exception e) {
            // Session destruction failed
        }
        
        // Deactivate session in database
        participantSessionRepository.deactivateByJanusSessionId(sessionId);
        
        // Deactivate ALL feeds for this user in this room
        participantFeedRepository.deactivateUserFeedsInRoom(
                currentUser.getId(), 
                roomId
        );
    }
    
    /**
     * End live streaming
     * Destroys all participant sessions and deactivates all feeds
     */
    @Transactional
    public LiveSessionResponse endLive(Long roomId) {
        User instructor = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        // Check if user is the instructor who started this session
        if (!liveSession.getInstructor().getId().equals(instructor.getId())) {
            throw new ForbiddenException("Only the instructor who started the session can end it");
        }
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // Destroy ALL participant sessions in this room
        List<ParticipantSession> activeSessions = participantSessionRepository
                .findByRoomIdAndIsActiveTrue(roomId);
        
        for (ParticipantSession session : activeSessions) {
            Long sessionId = session.getJanusSessionId();
            
            try {
                // Stop keepalive
                stopKeepalive(sessionId);
                
                // Destroy session in Janus
                janusService.destroySession(sessionId);
            } catch (Exception e) {
                // Session destruction failed
            }
        }
        
        // Deactivate all participant sessions in database
        participantSessionRepository.deactivateAllSessionsInRoom(roomId);
        
        // Destroy room in Janus
        janusService.destroyRoom(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                roomId
        );
        
        // Destroy instructor's main session in Janus
        janusService.destroySession(liveSession.getJanusSessionId());
        
        // Stop keepalive timer for instructor's main session
        stopKeepalive(liveSession.getJanusSessionId());
        
        // Stop all keepalive timers (cleanup any remaining)
        stopAllKeepalives();
        
        // Deactivate all participant feeds in this room
        participantFeedRepository.deactivateAllFeedsInRoom(roomId);

        // Update session status
        liveSession.setStatus(LiveSession.LiveStatus.ENDED);
        liveSession.setEndedAt(OffsetDateTime.now());
        liveSession = liveSessionRepository.save(liveSession);
        
        return liveSessionMapper.toResponse(liveSession);
    }
    
    /**
     * Get my feeds in a room
     */
    public MyFeedsResponse getMyFeeds(Long roomId) {
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        // Get all active feeds for current user
        List<ParticipantFeed> feeds = participantFeedRepository.findByUserAndRoomIdAndIsActiveTrue(
                currentUser, 
                roomId
        );
        
        List<MyFeedsResponse.FeedInfo> feedInfos = feeds.stream()
                .map(feed -> MyFeedsResponse.FeedInfo.builder()
                        .feedId(feed.getFeedId())
                        .feedType(feed.getFeedType())
                        .displayName(feed.getDisplayName())
                        .isActive(feed.getIsActive())
                        .sessionId(feed.getSessionId())
                        .handleId(feed.getHandleId())
                        .build())
                .toList();
        
        return MyFeedsResponse.builder()
                .roomId(roomId)
                .feeds(feedInfos)
                .build();
    }
    
    /**
     * Get live session info
     */
    public LiveSessionResponse getLiveSession(Long roomId) {
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        return liveSessionMapper.toResponse(liveSession);
    }

    /**
     * Get list of participants in a room from participant_sessions table
     * Trả ra userId và tên lúc join phòng để tiện cho việc kick
     */
    public RoomParticipantResponse getRoomParticipants(Long roomId) {
        // Ensure live session exists
        liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));

        List<ParticipantSession> activeSessions = participantSessionRepository.findByRoomIdAndIsActiveTrue(roomId);

        List<RoomParticipantResponse.ParticipantInfo> participantInfos = activeSessions.stream()
                .map(session -> RoomParticipantResponse.ParticipantInfo.builder()
                        .userId(session.getUser().getId())
                        .displayName(session.getDisplayName() != null
                                ? session.getDisplayName()
                                : session.getUser().getFullName())
                        .build())
                .toList();

        return RoomParticipantResponse.builder()
                .roomId(roomId)
                .participants(participantInfos)
                .build();
    }
    
    /**
     * Get all completed recordings of a batch
     * Returns list of recordings with objectName (frontend will get presigned URL via /uploads/get-url)
     */
    @Transactional(readOnly = true)
    public BatchRecordingsResponse getBatchRecordings(UUID batchId) {
        User currentUser = getCurrentUser();
        
        // Validate batch exists
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new DataNotFoundException("Batch not found with id: " + batchId));
        
        // Check if user is enrolled in batch or is an instructor
        boolean isInstructor = batch.getInstructors().stream()
                .anyMatch(bi -> bi.getInstructor().getId().equals(currentUser.getId()));
        boolean isEnrolled = batchEnrollmentRepository.existsByUserIdAndBatchId(currentUser.getId(), batchId);
        
        if (!isInstructor && !isEnrolled) {
            throw new ForbiddenException("You are not authorized to view recordings for this batch");
        }
        
        // Get all completed recordings for this batch
        List<LiveSession> completedSessions = liveSessionRepository.findCompletedRecordingsByBatchId(batchId);
        
        List<BatchRecordingsResponse.RecordingInfo> recordings = completedSessions.stream()
                .map(session -> BatchRecordingsResponse.RecordingInfo.builder()
                        .sessionId(session.getId())
                        .roomId(session.getRoomId())
                        .title(session.getTitle())
                        .description(session.getDescription())
                        .objectName(session.getFinalVideoObjectName())
                        .durationSeconds(session.getRecordingDuration())
                        .recordedAt(session.getStartedAt())
                        .instructorName(session.getInstructor() != null 
                                ? session.getInstructor().getFullName() 
                                : null)
                        .build())
                .collect(Collectors.toList());
        
        return BatchRecordingsResponse.builder()
                .batchId(batch.getId())
                .batchTitle(batch.getTitle())
                .batchSlug(batch.getSlug())
                .recordings(recordings)
                .build();
    }
    
    private Long generateRoomId() {
        return (long) (100000 + random.nextInt(900000));
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + email));
    }
    

    /**
     * Get current feed IDs from Janus participant list
     * Used to detect new feeds by comparing before/after join
     */
    private java.util.Set<Long> getCurrentFeedIds(Long roomId) {
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId).orElse(null);
        if (liveSession == null) {
            return new java.util.HashSet<>();
        }
        
        try {
            ParticipantListResponse participantList = janusService.listParticipants(
                    liveSession.getJanusSessionId(),
                    liveSession.getJanusHandleId(),
                    roomId
            );
            
            if (participantList.getParticipants() != null) {
                return participantList.getParticipants().stream()
                        .map(ParticipantListResponse.Participant::getId)
                        .collect(java.util.stream.Collectors.toSet());
            }
        } catch (Exception e) {
            // Ignore error, return empty set
        }
        
        return new java.util.HashSet<>();
    }
    
    /**
     * Poll Janus listParticipants to get feedId for a newly joined publisher
     * 
     * we compare feedIds before and after join to find the new one.
     * 
     * @param roomId The room ID
     * @param displayName The display name (for validation)
     * @param existingFeedIds Feed IDs that existed BEFORE join (to exclude)
     * @param timeoutMs Maximum time to wait in milliseconds
     * @return The feedId if found, null otherwise
     */
    private Long pollForNewFeedId(Long roomId, String displayName, java.util.Set<Long> existingFeedIds, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        int pollIntervalMs = 200; // Poll every 200ms
        
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId).orElse(null);
        if (liveSession == null) {
            return null;
        }
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                ParticipantListResponse participantList = janusService.listParticipants(
                        liveSession.getJanusSessionId(),
                        liveSession.getJanusHandleId(),
                        roomId
                );
                
                if (participantList.getParticipants() != null) {
                    // Find NEW feedId that wasn't in existingFeedIds
                    for (ParticipantListResponse.Participant p : participantList.getParticipants()) {
                        // Must be a new feedId AND match displayName for extra validation
                        if (!existingFeedIds.contains(p.getId()) && displayName.equals(p.getDisplay())) {
                            return p.getId();
                        }
                    }
                }
                
                Thread.sleep(pollIntervalMs);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                // Continue polling despite error
            }
        }
        
        return null;
    }
    
    
    private void startKeepalive(Long sessionId) {
        stopKeepalive(sessionId); // ensure previous timer cleared
        Timer timer = new Timer("janus-keepalive-" + sessionId, true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    janusService.keepAlive(sessionId);
                } catch (Exception ex) {
                    // Keepalive failed
                }
            }
        }, 30000L, 30000L); // start after 30s, repeat every 30s
        sessionKeepaliveTimers.put(sessionId, timer);
    }

    private void stopKeepalive(Long sessionId) {
        Timer t = sessionKeepaliveTimers.remove(sessionId);
        if (t != null) {
            t.cancel();
        }
    }

    private void stopAllKeepalives() {
        sessionKeepaliveTimers.forEach((sessionId, timer) -> timer.cancel());
        sessionKeepaliveTimers.clear();
    }
}

