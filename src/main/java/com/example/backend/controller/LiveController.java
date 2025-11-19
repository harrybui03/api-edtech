package com.example.backend.controller;

import com.example.backend.dto.request.live.*;
import com.example.backend.dto.response.live.*;
import com.example.backend.service.LiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/live")
@RequiredArgsConstructor
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
        LiveSessionResponse response = liveService.startLive(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Join live streaming room
     */
    @PostMapping("/join")
    @Operation(summary = "Join live streaming", description = "Join a live streaming session as publisher or subscriber")
    public ResponseEntity<JanusResponse> joinLive(@Valid @RequestBody JoinLiveRequest request) {
        JanusResponse response = liveService.joinLive(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Publish stream (camera/microphone)
     */
    @PostMapping("/publish")
    @Operation(summary = "Publish stream", description = "Publish video/audio stream from camera/microphone to the live session. Returns SDP answer for WebRTC.")
    public ResponseEntity<PublishStreamResponse> publishStream(@Valid @RequestBody PublishStreamRequest request) {
        PublishStreamResponse response = liveService.publishStream(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Publish screen share stream
     */
    @PostMapping("/publish-screen")
    @Operation(summary = "Publish screen share", description = "Publish screen sharing stream to the live session. Returns SDP answer for WebRTC.")
    public ResponseEntity<PublishStreamResponse> publishScreenShare(@Valid @RequestBody PublishStreamRequest request) {
        PublishStreamResponse response = liveService.publishScreenShare(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unpublish stream (camera/microphone)
     */
    @PostMapping("/unpublish/{roomId}")
    @Operation(summary = "Unpublish stream", description = "Stop publishing camera/microphone stream")
    public ResponseEntity<JanusResponse> unpublishStream(@PathVariable Long roomId) {
        JanusResponse response = liveService.unpublishStream(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unpublish screen share stream
     */
    @PostMapping("/unpublish-screen/{roomId}")
    @Operation(summary = "Unpublish screen share", description = "Stop publishing screen share stream independently from camera")
    public ResponseEntity<JanusResponse> unpublishScreenShare(@PathVariable Long roomId) {
        JanusResponse response = liveService.unpublishScreenShare(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kick participant
     */
    @PostMapping("/kick")
    @PreAuthorize("hasRole('COURSE_CREATOR')")
    @Operation(summary = "Kick participant", description = "Instructor kicks a participant from the live session")
    public ResponseEntity<JanusResponse> kickParticipant(@Valid @RequestBody KickParticipantRequest request) {
        JanusResponse response = liveService.kickParticipant(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Leave room (for participants)
     */
    @PostMapping("/leave/{roomId}")
    @Operation(summary = "Leave room", description = "Participant leaves the room and cleans up their main session (disconnects all feeds)")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId) {
        liveService.leaveRoom(roomId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/end/{roomId}")
    @PreAuthorize("hasRole('COURSE_CREATOR')")
    @Operation(summary = "End live streaming", description = "Instructor ends the live streaming session")
    public ResponseEntity<LiveSessionResponse> endLive(@PathVariable Long roomId) {
        LiveSessionResponse response = liveService.endLive(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get live session info
     */
    @GetMapping("/session/{roomId}")
    @Operation(summary = "Get live session info", description = "Get information about a live session")
    public ResponseEntity<LiveSessionResponse> getLiveSession(@PathVariable Long roomId) {
        LiveSessionResponse response = liveService.getLiveSession(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List participants (publishers) in room
     * Subscriber dùng để lấy danh sách publishers và feed IDs
     */
    @GetMapping("/participants/{roomId}")
    @Operation(summary = "List participants", description = "Get list of all participants (publishers) in a live session. Returns feed IDs for subscribers. Use excludeFeedId to filter out your own stream.")
    public ResponseEntity<ParticipantListResponse> listParticipants(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long excludeFeedId) {
        ParticipantListResponse response = liveService.listParticipants(roomId, excludeFeedId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Subscribe to a publisher's stream
     * Step 1: Configure subscriber - Janus trả về SDP offer
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to stream", description = "Subscribe to a publisher's stream. Returns SDP offer that subscriber needs to create answer from.")
    public ResponseEntity<SubscribeResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
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
        JanusResponse response = liveService.startSubscriber(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Keepalive a Janus session
     */
    @PostMapping("/keepalive/{sessionId}")
    @Operation(summary = "Send keepalive", description = "Send keepalive to Janus to prevent session timeout")
    public ResponseEntity<JanusResponse> keepAlive(@PathVariable Long sessionId) {
        JanusResponse response = liveService.keepAlive(sessionId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get my feeds in a room
     */
    @GetMapping("/my-feeds/{roomId}")
    @Operation(summary = "Get my feeds", description = "Get all active feeds (camera, screen) for current user in a room")
    public ResponseEntity<MyFeedsResponse> getMyFeeds(@PathVariable Long roomId) {
        MyFeedsResponse response = liveService.getMyFeeds(roomId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get recording for a live session
     */
    @GetMapping("/recording/{roomId}")
    @Operation(summary = "Get recording", description = "Get recording URL and status for a live session")
    public ResponseEntity<RecordingResponse> getRecording(@PathVariable Long roomId) {
        RecordingResponse response = liveService.getRecording(roomId);
        return ResponseEntity.ok(response);
    }
}

