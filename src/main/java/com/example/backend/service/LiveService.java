package com.example.backend.service;

import com.example.backend.dto.request.live.*;
import com.example.backend.dto.response.live.*;
import com.example.backend.entity.Batch;
import com.example.backend.entity.LiveSession;
import com.example.backend.entity.User;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.mapper.LiveSessionMapper;
import com.example.backend.repository.BatchRepository;
import com.example.backend.repository.LiveSessionRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveService {
    
    private final JanusService janusService;
    private final LiveSessionRepository liveSessionRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final LiveSessionMapper liveSessionMapper;
    
    private final Random random = new Random();

    // Keepalive timers for Janus sessions started by backend (publisher main session)
    private final Map<Long, Timer> sessionKeepaliveTimers = new ConcurrentHashMap<>();
    
    /**
     * Bắt đầu live streaming
     */
    @Transactional
    public LiveSessionResponse startLive(StartLiveRequest request) {
        log.info("Starting live for batch: {}", request.getBatchId());
        
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
        
        log.info("Live session started successfully with room ID: {}", roomId);
        
        return liveSessionMapper.toResponse(liveSession);
    }
    
    /**
     * Join live streaming
     */
    @Transactional
    public JanusResponse joinLive(JoinLiveRequest request) {
        log.info("Joining live room: {}", request.getRoomId());
        
        User currentUser = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // For simplicity, create a new session and handle for each participant
        // In production, you might want to reuse sessions
        JanusResponse sessionResponse = janusService.createSession();
        Long sessionId = sessionResponse.getData() != null 
                ? ((Number) sessionResponse.getData().get("id")).longValue() 
                : null;
        
        if (sessionId == null) {
            throw new RuntimeException("Failed to create Janus session");
        }
        
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long handleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (handleId == null) {
            throw new RuntimeException("Failed to attach plugin");
        }
        
        // Join room
        // Use display name from request if provided, otherwise use user's full name
        String displayName = (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty())
                ? request.getDisplayName().trim()
                : currentUser.getFullName();
        
        JanusResponse joinResponse = janusService.joinRoom(
                sessionId, 
                handleId, 
                request.getRoomId(), 
                request.getPtype(),
                displayName
        );
        
        // Start keepalive for this session (both publisher and subscriber)
        startKeepalive(sessionId);
        
        // Add sessionId and handleId to response for frontend to use in publish
        joinResponse.setSessionId(sessionId);
        joinResponse.setHandleId(handleId);
        
        log.info("User {} joined room {} as {} (session: {}, handle: {})", 
                currentUser.getFullName(), request.getRoomId(), request.getPtype(), sessionId, handleId);
        
        return joinResponse;
    }
    
    /**
     * Publish stream (camera/microphone)
     */
    public PublishStreamResponse publishStream(PublishStreamRequest request) {
        String streamType = request.getStreamType() != null ? request.getStreamType() : "camera";
        log.info("Publishing {} stream to room: {}", streamType, request.getRoomId());
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // Use sessionId/handleId from request (from Join) if provided
        // Otherwise use from liveSession (from Start Live - for instructor direct publish)
        Long sessionId = request.getSessionId() != null 
                ? request.getSessionId() 
                : liveSession.getJanusSessionId();
        Long handleId = request.getHandleId() != null 
                ? request.getHandleId() 
                : liveSession.getJanusHandleId();
        
        log.info("Publishing with sessionId: {}, handleId: {}", sessionId, handleId);
        
        JanusResponse janusResponse = janusService.publishStream(
                sessionId, 
                handleId, 
                request.getSdp()
        );
        
        // Extract SDP answer from jsep
        String sdpAnswer = null;
        String type = null;
        if (janusResponse.getJsep() != null) {
            type = (String) janusResponse.getJsep().get("type");
            sdpAnswer = (String) janusResponse.getJsep().get("sdp");
        }
        
        return PublishStreamResponse.builder()
                .sdpAnswer(sdpAnswer)
                .type(type)
                .sessionId(sessionId)
                .handleId(handleId)
                .error(janusResponse.getError())
                .errorCode(janusResponse.getErrorCode())
                .build();
    }
    
    /**
     * Publish screen share stream
     * Screen share cần session/handle riêng để có thể bật/tắt độc lập với camera
     */
    public PublishStreamResponse publishScreenShare(PublishStreamRequest request) {
        log.info("Publishing screen share stream to room: {}", request.getRoomId());
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.PUBLISHED) {
            throw new IllegalStateException("Live session is not published");
        }
        
        // Tạo session và handle riêng cho screen share
        // Điều này cho phép bật/tắt screen share độc lập với camera stream
        JanusResponse sessionResponse = janusService.createSession();
        Long screenSessionId = sessionResponse.getData() != null 
                ? ((Number) sessionResponse.getData().get("id")).longValue() 
                : null;
        
        if (screenSessionId == null) {
            throw new RuntimeException("Failed to create Janus session for screen share");
        }
        
        JanusResponse attachResponse = janusService.attachPlugin(screenSessionId);
        Long screenHandleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (screenHandleId == null) {
            throw new RuntimeException("Failed to attach plugin for screen share");
        }
        
        // Join room với screen session/handle riêng
        User currentUser = getCurrentUser();
        String displayName = currentUser.getFullName() + " (Screen)";
        janusService.joinRoom(screenSessionId, screenHandleId, request.getRoomId(), "publisher", displayName);
        
        // Start keepalive for screen session
        startKeepalive(screenSessionId);
        
        // Publish screen stream với session/handle riêng
        JanusResponse janusResponse = janusService.publishStream(
                screenSessionId, 
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
        
        log.info("Screen share published with sessionId: {}, handleId: {} (keepalive started)", screenSessionId, screenHandleId);
        
        return PublishStreamResponse.builder()
                .sdpAnswer(sdpAnswer)
                .type(type)
                .sessionId(screenSessionId) // Trả về sessionId riêng của screen
                .handleId(screenHandleId)   // Trả về handleId riêng của screen
                .error(janusResponse.getError())
                .errorCode(janusResponse.getErrorCode())
                .build();
    }
    
    /**
     * Unpublish stream (camera/microphone)
     */
    public JanusResponse unpublishStream(Long roomId) {
        log.info("Unpublishing camera stream from room: {}", roomId);
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        return janusService.unpublishStream(
                liveSession.getJanusSessionId(), 
                liveSession.getJanusHandleId()
        );
    }
    
    /**
     * Unpublish screen share stream
     * Sử dụng session/handle riêng của screen để tắt độc lập
     */
    public JanusResponse unpublishScreenShare(UnpublishScreenRequest request) {
        log.info("Unpublishing screen share from room: {}", request.getRoomId());
        
        // Validate live session exists
        liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        // Stop keepalive for screen session
        stopKeepalive(request.getScreenSessionId());
        
        // Unpublish screen stream
        JanusResponse unpublishResponse = janusService.unpublishStream(
                request.getScreenSessionId(),
                request.getScreenHandleId()
        );
        
        // Destroy screen session
        janusService.destroySession(request.getScreenSessionId());
        
        log.info("Screen share unpublished, session destroyed, keepalive stopped");
        
        return unpublishResponse;
    }
    
    /**
     * Kick participant
     */
    public JanusResponse kickParticipant(KickParticipantRequest request) {
        log.info("Kicking participant {} from room {}", request.getParticipantId(), request.getRoomId());
        
        User instructor = getCurrentUser();
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        // Check if user is the instructor who started this session
        if (!liveSession.getInstructor().getId().equals(instructor.getId())) {
            throw new ForbiddenException("Only the instructor who started the session can kick participants");
        }
        
        return janusService.kickParticipant(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                request.getRoomId(),
                request.getParticipantId()
        );
    }
    
    /**
     * List participants (publishers) in room
     * Subscriber dùng để lấy danh sách publishers và feed IDs
     */
    public ParticipantListResponse listParticipants(Long roomId) {
        log.info("Listing participants in room: {}", roomId);
        
        // Validate live session exists
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        return janusService.listParticipants(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                roomId
        );
    }
    
    /**
     * Subscribe to a publisher's stream
     * Step 1: Create new handle for this specific feed and get SDP offer
     * 
     * Note: Mỗi feed cần 1 handle riêng theo Janus VideoRoom design
     */
    public SubscribeResponse subscribe(SubscribeRequest request) {
        log.info("Subscribing to feed {} in room {}", request.getFeedId(), request.getRoomId());
        
        // Validate live session exists
        liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        // Create new session and handle for this specific feed
        // (Janus VideoRoom requires separate handle per subscribed feed)
        JanusResponse sessionResponse = janusService.createSession();
        Long sessionId = sessionResponse.getData() != null 
                ? ((Number) sessionResponse.getData().get("id")).longValue() 
                : null;
        
        if (sessionId == null) {
            throw new RuntimeException("Failed to create Janus session for subscriber");
        }
        
        JanusResponse attachResponse = janusService.attachPlugin(sessionId);
        Long handleId = attachResponse.getData() != null 
                ? ((Number) attachResponse.getData().get("id")).longValue() 
                : null;
        
        if (handleId == null) {
            throw new RuntimeException("Failed to attach plugin for subscriber");
        }
        
        // Start keepalive for this subscriber session
        startKeepalive(sessionId);
        
        log.info("Created subscriber session: {}, handle: {} for feed: {}", sessionId, handleId, request.getFeedId());
        
        // Join as subscriber with feed - Janus sẽ trả về SDP offer
        JanusResponse configureResponse = janusService.configureSubscriber(
                sessionId,
                handleId,
                request.getRoomId(),  // Pass room ID
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
                .sessionId(sessionId)  // Return new session
                .handleId(handleId)    // Return new handle
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
        log.info("Starting subscriber with SDP answer");
        
        return janusService.startSubscriber(request.getSessionId(), request.getHandleId(), request.getSdpAnswer());
    }

    /**
     * Send keepalive for a given Janus session
     */
    public JanusResponse keepAlive(Long sessionId) {
        log.info("Keepalive for session {}", sessionId);
        return janusService.keepAlive(sessionId);
    }
    
    /**
     * Leave room (for participants to clean up their session)
     */
    public void leaveRoom(Long sessionId) {
        log.info("Participant leaving room, cleaning up session: {}", sessionId);
        
        // Stop keepalive for this session
        stopKeepalive(sessionId);
        
        // Destroy session in Janus
        try {
            janusService.destroySession(sessionId);
        } catch (Exception e) {
            log.warn("Failed to destroy session {} in Janus: {}", sessionId, e.getMessage());
        }
        
        log.info("Participant left room successfully");
    }
    
    /**
     * End live streaming
     */
    @Transactional
    public LiveSessionResponse endLive(Long roomId) {
        log.info("Ending live session for room: {}", roomId);
        
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
        
        // Destroy room in Janus
        janusService.destroyRoom(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                roomId
        );
        
        // Destroy session in Janus
        janusService.destroySession(liveSession.getJanusSessionId());
        
        // Stop keepalive timer for main session
        stopKeepalive(liveSession.getJanusSessionId());
        
        // Stop all keepalive timers (for all participants)
        stopAllKeepalives();

        // Update session status
        liveSession.setStatus(LiveSession.LiveStatus.ENDED);
        liveSession.setEndedAt(OffsetDateTime.now());
        liveSession = liveSessionRepository.save(liveSession);
        
        log.info("Live session ended successfully");
        
        return liveSessionMapper.toResponse(liveSession);
    }
    
    /**
     * Get live session info
     */
    public LiveSessionResponse getLiveSession(Long roomId) {
        log.info("Getting live session info for room: {}", roomId);
        
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        return liveSessionMapper.toResponse(liveSession);
    }
    
    private Long generateRoomId() {
        return (long) (100000 + random.nextInt(900000));
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + email));
    }

    private void startKeepalive(Long sessionId) {
        stopKeepalive(sessionId); // ensure previous timer cleared
        Timer timer = new Timer("janus-keepalive-" + sessionId, true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    janusService.keepAlive(sessionId);
                    log.debug("Keepalive sent for session {}", sessionId);
                } catch (Exception ex) {
                    log.warn("Keepalive failed for session {}: {}", sessionId, ex.getMessage());
                }
            }
        }, 30000L, 30000L); // start after 30s, repeat every 30s
        sessionKeepaliveTimers.put(sessionId, timer);
    }

    private void stopKeepalive(Long sessionId) {
        Timer t = sessionKeepaliveTimers.remove(sessionId);
        if (t != null) {
            t.cancel();
            log.debug("Keepalive stopped for session {}", sessionId);
        }
    }

    private void stopAllKeepalives() {
        sessionKeepaliveTimers.forEach((sessionId, timer) -> {
            timer.cancel();
            log.debug("Keepalive stopped for session {}", sessionId);
        });
        sessionKeepaliveTimers.clear();
        log.info("All keepalive timers stopped");
    }
}

