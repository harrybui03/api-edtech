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
import java.util.Random;

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
        
        // Check if there's already an active session for this batch
        liveSessionRepository.findByBatchIdAndStatus(batch.getId(), LiveSession.LiveStatus.ACTIVE)
                .stream()
                .findFirst()
                .ifPresent(session -> {
                    throw new IllegalStateException("There is already an active live session for this batch");
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
                .status(LiveSession.LiveStatus.ACTIVE)
                .title(request.getTitle())
                .description(request.getDescription())
                .startedAt(OffsetDateTime.now())
                .build();
        
        liveSession = liveSessionRepository.save(liveSession);
        
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
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.ACTIVE) {
            throw new IllegalStateException("Live session is not active");
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
        String displayName = currentUser.getFullName();
        JanusResponse joinResponse = janusService.joinRoom(
                sessionId, 
                handleId, 
                request.getRoomId(), 
                request.getPtype(),
                displayName
        );
        
        log.info("User {} joined room {} as {}", currentUser.getFullName(), request.getRoomId(), request.getPtype());
        
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
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.ACTIVE) {
            throw new IllegalStateException("Live session is not active");
        }
        
        JanusResponse janusResponse = janusService.publishStream(
                liveSession.getJanusSessionId(), 
                liveSession.getJanusHandleId(), 
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
                .sessionId(janusResponse.getSessionId())
                .handleId(janusResponse.getHandleId())
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
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.ACTIVE) {
            throw new IllegalStateException("Live session is not active");
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
        
        // Unpublish screen stream
        JanusResponse unpublishResponse = janusService.unpublishStream(
                request.getScreenSessionId(),
                request.getScreenHandleId()
        );
        
        // Destroy screen session
        janusService.destroySession(request.getScreenSessionId());
        
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
     * Step 1: Configure subscriber - Janus trả về SDP offer
     */
    public SubscribeResponse subscribe(SubscribeRequest request) {
        log.info("Subscribing to feed {} in room {}", request.getFeedId(), request.getRoomId());
        
        // Validate live session exists
        liveSessionRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + request.getRoomId()));
        
        // Configure subscriber - Janus sẽ trả về SDP offer
        JanusResponse configureResponse = janusService.configureSubscriber(
                request.getSessionId(),
                request.getHandleId(),
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
                .sessionId(request.getSessionId())
                .handleId(request.getHandleId())
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
        
        if (liveSession.getStatus() != LiveSession.LiveStatus.ACTIVE) {
            throw new IllegalStateException("Live session is not active");
        }
        
        // Destroy room in Janus
        janusService.destroyRoom(
                liveSession.getJanusSessionId(),
                liveSession.getJanusHandleId(),
                roomId
        );
        
        // Destroy session in Janus
        janusService.destroySession(liveSession.getJanusSessionId());
        
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
}

