package com.example.backend.controller;

import com.example.backend.dto.request.live.*;
import com.example.backend.dto.response.live.*;
import com.example.backend.service.LiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/live")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Live Streaming", description = "APIs for live streaming management")
@SecurityRequirement(name = "bearerAuth")
public class LiveController {
    
    private final LiveService liveService;
    
    /**
     * Bắt đầu live streaming
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('COURSE_CREATOR')")
    @Operation(summary = "Start live streaming", description = "Instructor starts a live streaming session for a batch")
    public ResponseEntity<LiveSessionResponse> startLive(@Valid @RequestBody StartLiveRequest request) {
        log.info("Request to start live for batch: {}", request.getBatchId());
        LiveSessionResponse response = liveService.startLive(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Join live streaming room
     */
    @PostMapping("/join")
    @Operation(summary = "Join live streaming", description = "Join a live streaming session as publisher or subscriber")
    public ResponseEntity<JanusResponse> joinLive(@Valid @RequestBody JoinLiveRequest request) {
        log.info("Request to join live room: {}", request.getRoomId());
        JanusResponse response = liveService.joinLive(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Publish stream (camera/microphone)
     */
    @PostMapping("/publish")
    @Operation(summary = "Publish stream", description = "Publish video/audio stream from camera/microphone to the live session. Returns SDP answer for WebRTC.")
    public ResponseEntity<PublishStreamResponse> publishStream(@Valid @RequestBody PublishStreamRequest request) {
        log.info("Request to publish stream to room: {}", request.getRoomId());
        PublishStreamResponse response = liveService.publishStream(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Publish screen share stream
     */
    @PostMapping("/publish-screen")
    @Operation(summary = "Publish screen share", description = "Publish screen sharing stream to the live session. Returns SDP answer for WebRTC.")
    public ResponseEntity<PublishStreamResponse> publishScreenShare(@Valid @RequestBody PublishStreamRequest request) {
        log.info("Request to publish screen share to room: {}", request.getRoomId());
        PublishStreamResponse response = liveService.publishScreenShare(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unpublish stream (camera/microphone)
     */
    @PostMapping("/unpublish/{roomId}")
    @Operation(summary = "Unpublish stream", description = "Stop publishing camera/microphone stream")
    public ResponseEntity<JanusResponse> unpublishStream(@PathVariable Long roomId) {
        log.info("Request to unpublish camera stream from room: {}", roomId);
        JanusResponse response = liveService.unpublishStream(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unpublish screen share stream
     */
    @PostMapping("/unpublish-screen")
    @Operation(summary = "Unpublish screen share", description = "Stop publishing screen share stream independently from camera")
    public ResponseEntity<JanusResponse> unpublishScreenShare(@Valid @RequestBody UnpublishScreenRequest request) {
        log.info("Request to unpublish screen share from room: {}", request.getRoomId());
        JanusResponse response = liveService.unpublishScreenShare(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kick participant
     */
    @PostMapping("/kick")
    @PreAuthorize("hasRole('COURSE_CREATOR')")
    @Operation(summary = "Kick participant", description = "Instructor kicks a participant from the live session")
    public ResponseEntity<JanusResponse> kickParticipant(@Valid @RequestBody KickParticipantRequest request) {
        log.info("Request to kick participant {} from room {}", request.getParticipantId(), request.getRoomId());
        JanusResponse response = liveService.kickParticipant(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * End live streaming
     */
    @PostMapping("/end/{roomId}")
    @PreAuthorize("hasRole('COURSE_CREATOR')")
    @Operation(summary = "End live streaming", description = "Instructor ends the live streaming session")
    public ResponseEntity<LiveSessionResponse> endLive(@PathVariable Long roomId) {
        log.info("Request to end live session for room: {}", roomId);
        LiveSessionResponse response = liveService.endLive(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get live session info
     */
    @GetMapping("/session/{roomId}")
    @Operation(summary = "Get live session info", description = "Get information about a live session")
    public ResponseEntity<LiveSessionResponse> getLiveSession(@PathVariable Long roomId) {
        log.info("Request to get live session info for room: {}", roomId);
        LiveSessionResponse response = liveService.getLiveSession(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List participants (publishers) in room
     * Subscriber dùng để lấy danh sách publishers và feed IDs
     */
    @GetMapping("/participants/{roomId}")
    @Operation(summary = "List participants", description = "Get list of all participants (publishers) in a live session. Returns feed IDs for subscribers.")
    public ResponseEntity<ParticipantListResponse> listParticipants(@PathVariable Long roomId) {
        log.info("Request to list participants in room: {}", roomId);
        ParticipantListResponse response = liveService.listParticipants(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Subscribe to a publisher's stream
     * Step 1: Configure subscriber - Janus trả về SDP offer
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to stream", description = "Subscribe to a publisher's stream. Returns SDP offer that subscriber needs to create answer from.")
    public ResponseEntity<SubscribeResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        log.info("Request to subscribe to feed {} in room {}", request.getFeedId(), request.getRoomId());
        SubscribeResponse response = liveService.subscribe(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Start subscriber with SDP answer
     * Step 2: Send SDP answer to start receiving stream
     */
    @PostMapping("/start-subscriber")
    @Operation(summary = "Start subscriber", description = "Send SDP answer to start receiving stream from publisher")
    public ResponseEntity<JanusResponse> startSubscriber(@Valid @RequestBody StartSubscriberRequest request) {
        log.info("Request to start subscriber");
        JanusResponse response = liveService.startSubscriber(request);
        return ResponseEntity.ok(response);
    }
}

