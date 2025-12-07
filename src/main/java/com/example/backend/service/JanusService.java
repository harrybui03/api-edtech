package com.example.backend.service;

import com.example.backend.dto.response.live.JanusResponse;
import com.example.backend.dto.response.live.ParticipantListResponse;
import com.example.backend.excecption.InternalServerError;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class JanusService {
    
    private final RestTemplate restTemplate;
    
    @Value("${janus.server.url:http://localhost:8088/janus}")
    private String janusServerUrl;
    
    /**
     * Tạo Janus session
     */
    public JanusResponse createSession() {
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "create");
        request.put("transaction", generateTransactionId());
        
        log.debug("Creating Janus session...");
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    janusServerUrl,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            JanusResponse result = mapToJanusResponse(body);
            
            log.info("Janus session created: {}", result.getSessionId());
            return result;
        } catch (Exception e) {
            log.error("Failed to create Janus session", e);
            throw new InternalServerError("Failed to create Janus session: " + e.getMessage());
        }
    }
    
    /**
     * Send keepalive to Janus session to prevent timeout
     */
    public JanusResponse keepAlive(Long sessionId) {
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "keepalive");
        request.put("transaction", generateTransactionId());

        String url = janusServerUrl + "/" + sessionId;

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return mapToJanusResponse(body);
        } catch (Exception e) {
            throw new InternalServerError("Failed to send keepalive: " + e.getMessage());
        }
    }

    /**
     * Attach plugin videoroom
     */
    public JanusResponse attachPlugin(Long sessionId) {
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "attach");
        request.put("plugin", "janus.plugin.videoroom");
        request.put("transaction", generateTransactionId());
        
        String url = janusServerUrl + "/" + sessionId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return mapToJanusResponse(body);
        } catch (Exception e) {
            throw new InternalServerError("Failed to attach plugin: " + e.getMessage());
        }
    }
    
    /**
     * Tạo room
     */
    public JanusResponse createRoom(Long sessionId, Long handleId, Long roomId) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "create");
        body.put("room", roomId);
        body.put("permanent", false);
        body.put("description", "Live streaming room " + roomId);
        body.put("is_private", false);
        body.put("publishers", 10);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to create room: " + e.getMessage());
        }
    }
    
    /**
     * Join room
     */
    public JanusResponse joinRoom(Long sessionId, Long handleId, Long roomId, String ptype, String displayName) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "join");
        body.put("room", roomId);
        body.put("ptype", ptype);
        body.put("display", displayName);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to join room: " + e.getMessage());
        }
    }
    
    /**
     * Publish stream
     */
    public JanusResponse publishStream(Long sessionId, Long handleId, String sdp) {
        log.info("Publishing stream: session={}, handle={}", sessionId, handleId);
        log.debug("SDP offer length: {}", sdp != null ? sdp.length() : 0);
        
        Map<String, Object> body = new HashMap<>();
        body.put("request", "publish");
        
        Map<String, Object> jsep = new HashMap<>();
        jsep.put("type", "offer");
        jsep.put("sdp", sdp);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        request.put("jsep", jsep);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try{
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            JanusResponse janusResponse = mapToJanusResponse(responseBody);
            
            // If we got ACK, need to wait for event with JSEP
            // For REST API, we can't easily get the event, so we need to poll or use long-polling
            if ("ack".equals(janusResponse.getJanus())) {
                // Try to get event (with retry)
                try {
                    // Poll up to 30 times with 1 second between each (total ~30 seconds max)
                    // Janus events usually arrive within 1-3 seconds
                    outerLoop:
                    for (int attempt = 0; attempt < 30; attempt++) {
                        Thread.sleep(1000); // Wait 1 second for Janus to process
                        
                        // Make another request to get pending events (maxev=10 returns array)
                        @SuppressWarnings("rawtypes")
                        ResponseEntity<List> eventResponse = restTemplate.getForEntity(
                            janusServerUrl + "/" + sessionId + "?maxev=10",
                            List.class
                        );
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> eventList = eventResponse.getBody();
                    
                        if (eventList == null || eventList.isEmpty()) {
                            continue; // No events yet, try again
                        }
                        
                        for (Map<String, Object> eventBody : eventList) {
                            if (eventBody == null) continue;
                            
                            String janusType = (String) eventBody.get("janus");
                            
                            // Check if this event is for our handle (but don't skip sender = 0 or null)
                            Long eventSender = null;
                            if (eventBody.containsKey("sender")) {
                                Object senderObj = eventBody.get("sender");
                                if (senderObj != null) {
                                    eventSender = ((Number) senderObj).longValue();
                                }
                            }
                            
                            // Only skip if sender is non-zero and doesn't match our handle
                            // Accept: sender = null, sender = 0, sender = handleId
                            if (eventSender != null && eventSender != 0 && !eventSender.equals(handleId)) {
                                continue; // Skip this event, try next one
                            }
                            
                            if ("event".equals(janusType)) {
                                // Check if this is a relevant publish event (has JSEP or configured)
                                boolean hasJsep = eventBody.containsKey("jsep");
                                boolean isPublishEvent = false;
                                boolean isUnpublishOrLeaveEvent = false;
                                
                                if (eventBody.containsKey("plugindata")) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> plugindata = (Map<String, Object>) eventBody.get("plugindata");
                                    if (plugindata != null && plugindata.containsKey("data")) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> data = (Map<String, Object>) plugindata.get("data");
                                        if (data != null) {
                                            // Skip unpublish/leave events - they are from previous requests
                                            isUnpublishOrLeaveEvent = data.containsKey("unpublished") || 
                                                                       data.containsKey("leaving");
                                            
                                            // Check if this is a publish success event
                                            isPublishEvent = data.containsKey("configured") || 
                                                           data.containsKey("publishers") ||
                                                           (data.containsKey("videoroom") && 
                                                            "event".equals(data.get("videoroom")) && 
                                                            !isUnpublishOrLeaveEvent);
                                        }
                                    }
                                }
                                
                                // Skip unpublish/leave events - they are not for publish request
                                if (isUnpublishOrLeaveEvent) {
                                    continue; // Skip this event, try next one
                                }
                                
                                // Only process if it's a publish event with JSEP
                                if (hasJsep || isPublishEvent) {
                                    janusResponse = mapToJanusResponse(eventBody);
                                    
                                    // Check if event contains error (but only for publish-related errors)
                                    if (janusResponse.getPlugindata() != null) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> pluginData = (Map<String, Object>) janusResponse.getPlugindata().get("data");
                                        if (pluginData != null && pluginData.containsKey("error_code")) {
                                            int errorCode = ((Number) pluginData.get("error_code")).intValue();
                                            // Error 435 = "Can't unpublish, not published" - skip this, it's from unpublish request
                                            if (errorCode == 435) {
                                                continue; // Skip this error, try next event
                                            }
                                            janusResponse.setError((String) pluginData.get("error"));
                                            janusResponse.setErrorCode(errorCode);
                                            break outerLoop; // Error found, stop polling
                                        }
                                    }
                                    
                                    if (janusResponse.getJsep() != null) {
                                        log.info("Received SDP answer from Janus after {} attempts", attempt + 1);
                                        break outerLoop; // Found JSEP, stop polling
                                    }
                                }
                            } else if ("error".equals(janusType)) {
                                // Check error code - skip 435 (unpublish error)
                                if (eventBody.containsKey("error")) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> error = (Map<String, Object>) eventBody.get("error");
                                    if (error != null && error.containsKey("code")) {
                                        int errorCode = ((Number) error.get("code")).intValue();
                                        if (errorCode == 435) {
                                            continue; // Skip unpublish error, try next event
                                        }
                                    }
                                }
                                janusResponse = mapToJanusResponse(eventBody);
                                log.warn("Janus publish error: {} (code: {})", janusResponse.getError(), janusResponse.getErrorCode());
                                break outerLoop; // Error, stop polling
                            }
                        }
                    } // end of for loop
                    
                    if (janusResponse.getJsep() == null && janusResponse.getError() == null) {
                        log.warn("Publish: No JSEP response received after 30 polling attempts");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Publish polling interrupted");
                } catch (Exception e) {
                    log.error("Error polling for publish event", e);
                }
            }
            
            return janusResponse;
        } catch (Exception e) {
            throw new InternalServerError("Failed to publish stream: " + e.getMessage());
        }
    }
    
    /**
     * Unpublish stream
     */
    public JanusResponse unpublishStream(Long sessionId, Long handleId) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "unpublish");
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to unpublish stream: " + e.getMessage());
        }
    }
    
    /**
     * Detach plugin handle (destroy handle but keep session)
     */
    public JanusResponse detachPlugin(Long sessionId, Long handleId) {
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "detach");
        request.put("transaction", generateTransactionId());
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to detach plugin: " + e.getMessage());
        }
    }
    
    /**
     * Kick participant
     */
    public JanusResponse kickParticipant(Long sessionId, Long handleId, Long roomId, Long participantId) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "kick");
        body.put("room", roomId);
        body.put("id", participantId);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to kick participant: " + e.getMessage());
        }
    }
    
    /**
     * Configure subscriber to receive stream from a publisher
     * Janus will return SDP offer in response
     * 
     * Note: For VideoRoom subscriber, we use "join" with feed parameter
     */
    public JanusResponse configureSubscriber(Long sessionId, Long handleId, Long roomId, Long feedId) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "join");
        body.put("room", roomId);  // Required!
        body.put("ptype", "subscriber");
        body.put("feed", feedId);
        body.put("offer_audio", true);
        body.put("offer_video", true);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            JanusResponse janusResponse = mapToJanusResponse(responseBody);
            
            // If we got ACK, need to wait for event with SDP offer (similar to publish)
            if ("ack".equals(janusResponse.getJanus())) {
                try {
                    // Poll up to 30 times with 1 second between each (total ~30 seconds max)
                    // Janus events usually arrive within 1-3 seconds
                    outerLoop:
                    for (int attempt = 0; attempt < 30; attempt++) {
                        Thread.sleep(1000); // Wait 1 second for Janus to process
                        
                        // maxev=10 returns array of events
                        @SuppressWarnings("rawtypes")
                        ResponseEntity<List> eventResponse = restTemplate.getForEntity(
                            janusServerUrl + "/" + sessionId + "?maxev=10",
                            List.class
                        );
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> eventList = eventResponse.getBody();
                    
                        if (eventList == null || eventList.isEmpty()) {
                            continue; // No events yet, try again
                        }
                        
                        for (Map<String, Object> eventBody : eventList) {
                            if (eventBody == null) continue;
                            
                            String janusType = (String) eventBody.get("janus");
                            
                            // Check if this event is for our handle (but don't skip sender = 0 or null)
                            Long eventSender = null;
                            if (eventBody.containsKey("sender")) {
                                Object senderObj = eventBody.get("sender");
                                if (senderObj != null) {
                                    eventSender = ((Number) senderObj).longValue();
                                }
                            }
                            
                            // Only skip if sender is non-zero and doesn't match our handle
                            // Accept: sender = null, sender = 0, sender = handleId
                            if (eventSender != null && eventSender != 0 && !eventSender.equals(handleId)) {
                                continue; // Skip this event, try next one
                            }
                            
                            if ("event".equals(janusType)) {
                                janusResponse = mapToJanusResponse(eventBody);
                                
                                // Check if event contains error
                                if (janusResponse.getPlugindata() != null) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> pluginData = (Map<String, Object>) janusResponse.getPlugindata().get("data");
                                    if (pluginData != null && pluginData.containsKey("error_code")) {
                                        janusResponse.setError((String) pluginData.get("error"));
                                        janusResponse.setErrorCode(((Number) pluginData.get("error_code")).intValue());
                                        break outerLoop;
                                    }
                                }
                                
                                if (janusResponse.getJsep() != null) {
                                    break outerLoop; // Found JSEP, stop polling
                                }
                            } else if ("error".equals(janusType)) {
                                janusResponse = mapToJanusResponse(eventBody);
                                break outerLoop;
                            }
                        }
                    } // end of for loop
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // Failed to get subscriber event
                }
            }
            
            return janusResponse;
        } catch (Exception e) {
            throw new InternalServerError("Failed to configure subscriber: " + e.getMessage());
        }
    }
    
    /**
     * Start subscriber (send SDP answer after receiving offer)
     */
    public JanusResponse startSubscriber(Long sessionId, Long handleId, String sdpAnswer) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "start");
        
        Map<String, Object> jsep = new HashMap<>();
        jsep.put("type", "answer");
        jsep.put("sdp", sdpAnswer);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        request.put("jsep", jsep);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to start subscriber: " + e.getMessage());
        }
    }
    
    /**
     * List participants
     */
    public ParticipantListResponse listParticipants(Long sessionId, Long handleId, Long roomId) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "listparticipants");
        body.put("room", roomId);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("plugindata")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> plugindata = (Map<String, Object>) responseBody.get("plugindata");
                if (plugindata.containsKey("data")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) plugindata.get("data");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> participants = (List<Map<String, Object>>) data.get("participants");
                    
                    List<ParticipantListResponse.Participant> participantList = new ArrayList<>();
                    if (participants != null) {
                        for (Map<String, Object> p : participants) {
                            ParticipantListResponse.Participant participant = ParticipantListResponse.Participant.builder()
                                    .id(((Number) p.get("id")).longValue())
                                    .display((String) p.get("display"))
                                    .publisher((Boolean) p.get("publisher"))
                                    .build();
                            participantList.add(participant);
                        }
                    }
                    
                    return ParticipantListResponse.builder()
                            .roomId(roomId)
                            .participants(participantList)
                            .build();
                }
            }
            
            return ParticipantListResponse.builder()
                    .roomId(roomId)
                    .participants(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            throw new InternalServerError("Failed to list participants: " + e.getMessage());
        }
    }
    
    /**
     * Destroy room
     */
    public JanusResponse destroyRoom(Long sessionId, Long handleId, Long roomId) {
        Map<String, Object> body = new HashMap<>();
        body.put("request", "destroy");
        body.put("room", roomId);
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "message");
        request.put("transaction", generateTransactionId());
        request.put("body", body);
        
        String url = janusServerUrl + "/" + sessionId + "/" + handleId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return mapToJanusResponse(responseBody);
        } catch (Exception e) {
            throw new InternalServerError("Failed to destroy room: " + e.getMessage());
        }
    }
    
    /**
     * Destroy session
     */
    public JanusResponse destroySession(Long sessionId) {
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "destroy");
        request.put("transaction", generateTransactionId());
        
        String url = janusServerUrl + "/" + sessionId;
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return mapToJanusResponse(body);
        } catch (Exception e) {
            throw new InternalServerError("Failed to destroy session: " + e.getMessage());
        }
    }
    
    private JanusResponse mapToJanusResponse(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return null;
        }
        
        JanusResponse janusResponse = new JanusResponse();
        janusResponse.setJanus((String) responseBody.get("janus"));
        janusResponse.setTransaction((String) responseBody.get("transaction"));
        
        // Handle session_id first (present in most responses)
        if (responseBody.containsKey("session_id")) {
            janusResponse.setSessionId(((Number) responseBody.get("session_id")).longValue());
        }
        
        // Handle sender (handle ID in event responses)
        if (responseBody.containsKey("sender")) {
            janusResponse.setHandleId(((Number) responseBody.get("sender")).longValue());
        }
        
        // Handle data object (contains ID for create session/attach plugin)
        if (responseBody.containsKey("data")) {
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                janusResponse.setData(data);
                
                if (data.containsKey("id")) {
                    Long id = ((Number) data.get("id")).longValue();
                    
                    // Determine if this is session ID or handle ID based on response type
                    // - Create session: data.id is sessionId
                    // - Attach plugin: data.id is handleId (session_id is separate field)
                    if (janusResponse.getSessionId() == null) {
                        // This is a create session response - data.id is session ID
                        janusResponse.setSessionId(id);
                    } else {
                        // This is an attach response - data.id is handle ID
                        janusResponse.setHandleId(id);
                    }
                }
            }
        }
        
        if (responseBody.containsKey("plugindata")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> plugindata = (Map<String, Object>) responseBody.get("plugindata");
            janusResponse.setPlugindata(plugindata);
        }
        
        if (responseBody.containsKey("jsep")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsep = (Map<String, Object>) responseBody.get("jsep");
            janusResponse.setJsep(jsep);
        }
        
        // Handle error object - fix ClassCastException
        if (responseBody.containsKey("error")) {
            Object errorObj = responseBody.get("error");
            if (errorObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> error = (Map<String, Object>) errorObj;
                janusResponse.setError((String) error.get("reason"));
                if (error.containsKey("code")) {
                    janusResponse.setErrorCode(((Number) error.get("code")).intValue());
                }
            } else if (errorObj instanceof String) {
                janusResponse.setError((String) errorObj);
            }
        }
        
        return janusResponse;
    }
    
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}


