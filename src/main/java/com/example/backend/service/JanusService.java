package com.example.backend.service;

import com.example.backend.dto.response.live.JanusResponse;
import com.example.backend.dto.response.live.ParticipantListResponse;
import com.example.backend.excecption.InternalServerError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JanusService {
    
    private final RestTemplate restTemplate;
    
    @Value("${janus.server.url:http://localhost:8088/janus}")
    private String janusServerUrl;
    
    /**
     * Tạo Janus session
     */
    public JanusResponse createSession() {
        log.info("Creating Janus session");
        
        Map<String, Object> request = new HashMap<>();
        request.put("janus", "create");
        request.put("transaction", generateTransactionId());
        
        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    janusServerUrl,
                    request,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return mapToJanusResponse(body);
        } catch (Exception e) {
            log.error("Error creating Janus session: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to create Janus session: " + e.getMessage());
        }
    }
    
    /**
     * Send keepalive to Janus session to prevent timeout
     */
    public JanusResponse keepAlive(Long sessionId) {
        log.info("Sending keepalive for session {}", sessionId);

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
            log.error("Error sending keepalive: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to send keepalive: " + e.getMessage());
        }
    }

    /**
     * Attach plugin videoroom
     */
    public JanusResponse attachPlugin(Long sessionId) {
        log.info("Attaching videoroom plugin to session: {}", sessionId);
        
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
            log.error("Error attaching plugin: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to attach plugin: " + e.getMessage());
        }
    }
    
    /**
     * Tạo room
     */
    public JanusResponse createRoom(Long sessionId, Long handleId, Long roomId) {
        log.info("Creating room {} in session {} with handle {}", roomId, sessionId, handleId);
        
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
            log.error("Error creating room: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to create room: " + e.getMessage());
        }
    }
    
    /**
     * Join room
     */
    public JanusResponse joinRoom(Long sessionId, Long handleId, Long roomId, String ptype, String displayName) {
        log.info("Joining room {} as {} with display name {}", roomId, ptype, displayName);
        
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
            log.error("Error joining room: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to join room: " + e.getMessage());
        }
    }
    
    /**
     * Publish stream
     */
    public JanusResponse publishStream(Long sessionId, Long handleId, String sdp) {
        log.info("Publishing stream in session {} with handle {}", sessionId, handleId);
        log.debug("SDP offer (first 200 chars): {}", sdp.substring(0, Math.min(200, sdp.length())));
        
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
        log.info("Publishing to URL: {}", url);
        
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
            
            log.info("Publish response - janus: {}, error: {}", janusResponse.getJanus(), janusResponse.getError());
            
            // If we got ACK, need to wait for event with JSEP
            // For REST API, we can't easily get the event, so we need to poll or use long-polling
            if ("ack".equals(janusResponse.getJanus())) {
                log.warn("Received ACK for publish, waiting for event with SDP answer...");
                // Try to get event (with retry and longer timeout)
                try {
                    // Retry up to 3 times with 500ms between each
                    for (int attempt = 0; attempt < 3; attempt++) {
                        Thread.sleep(500); // Wait 500ms for Janus to process
                        
                        log.info("Polling for events (attempt {}/3)...", attempt + 1);
                        
                        // Make another request to get pending events
                        @SuppressWarnings("rawtypes")
                        ResponseEntity<Map> eventResponse = restTemplate.getForEntity(
                            janusServerUrl + "/" + sessionId + "?maxev=1",
                            Map.class
                        );
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eventBody = eventResponse.getBody();
                    
                    if (eventBody != null) {
                        String janusType = (String) eventBody.get("janus");
                        log.info("Polled event type: {}, full response: {}", janusType, eventBody);
                        
                        if ("event".equals(janusType)) {
                            janusResponse = mapToJanusResponse(eventBody);
                            
                            log.info("After mapping - JSEP present: {}, plugindata present: {}", 
                                janusResponse.getJsep() != null, 
                                janusResponse.getPlugindata() != null);
                            
                            // Check if event contains error
                            if (janusResponse.getPlugindata() != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> pluginData = (Map<String, Object>) janusResponse.getPlugindata().get("data");
                                if (pluginData != null && pluginData.containsKey("error_code")) {
                                    log.error("Janus plugin error: {} - {}", 
                                        pluginData.get("error_code"), 
                                        pluginData.get("error"));
                                    janusResponse.setError((String) pluginData.get("error"));
                                    janusResponse.setErrorCode(((Number) pluginData.get("error_code")).intValue());
                                } else {
                                    log.info("Event has plugindata but no error");
                                }
                            }
                            
                            if (janusResponse.getJsep() != null) {
                                log.info("Got event response with JSEP - type: {}", janusResponse.getJsep().get("type"));
                                break; // Found JSEP, stop polling
                            } else {
                                log.warn("Event response mapped but JSEP is still null, will retry...");
                            }
                        } else if ("error".equals(janusType)) {
                            janusResponse = mapToJanusResponse(eventBody);
                            log.error("Janus error response: {}", janusResponse.getError());
                            break; // Error, stop polling
                        } else {
                            log.warn("Unexpected janus type: {}, will retry...", janusType);
                        }
                    } else {
                        log.warn("Event body is null after polling, will retry...");
                    }
                    } // end of for loop
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Interrupted while waiting for event: {}", e.getMessage());
                } catch (Exception e) {
                    log.warn("Failed to get event: {}", e.getMessage());
                }
            }
            
            if (janusResponse.getJsep() != null) {
                log.info("Publish successful - SDP answer received");
            } else {
                log.warn("Publish response has no JSEP (SDP answer) - may need to wait longer");
            }
            
            return janusResponse;
        } catch (Exception e) {
            log.error("Error publishing stream: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to publish stream: " + e.getMessage());
        }
    }
    
    /**
     * Unpublish stream
     */
    public JanusResponse unpublishStream(Long sessionId, Long handleId) {
        log.info("Unpublishing stream in session {} with handle {}", sessionId, handleId);
        
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
            log.error("Error unpublishing stream: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to unpublish stream: " + e.getMessage());
        }
    }
    
    /**
     * Kick participant
     */
    public JanusResponse kickParticipant(Long sessionId, Long handleId, Long roomId, Long participantId) {
        log.info("Kicking participant {} from room {}", participantId, roomId);
        
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
            log.error("Error kicking participant: {}", e.getMessage(), e);
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
        log.info("Subscribing to feed: {} in room: {} (using join request)", feedId, roomId);
        
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
            
            log.info("Configure subscriber response - janus: {}, error: {}", janusResponse.getJanus(), janusResponse.getError());
            
            // If we got ACK, need to wait for event with SDP offer (similar to publish)
            if ("ack".equals(janusResponse.getJanus())) {
                log.warn("Received ACK for configure subscriber, waiting for event with SDP offer...");
                try {
                    // Retry up to 3 times with 500ms between each
                    for (int attempt = 0; attempt < 3; attempt++) {
                        Thread.sleep(500);
                        
                        log.info("Polling for subscriber events (attempt {}/3)...", attempt + 1);
                        
                        @SuppressWarnings("rawtypes")
                        ResponseEntity<Map> eventResponse = restTemplate.getForEntity(
                            janusServerUrl + "/" + sessionId + "?maxev=1",
                            Map.class
                        );
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eventBody = eventResponse.getBody();
                    
                        if (eventBody != null) {
                            String janusType = (String) eventBody.get("janus");
                            log.info("Polled subscriber event type: {}", janusType);
                            
                            if ("event".equals(janusType)) {
                                janusResponse = mapToJanusResponse(eventBody);
                                
                                // Check if event contains error
                                if (janusResponse.getPlugindata() != null) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> pluginData = (Map<String, Object>) janusResponse.getPlugindata().get("data");
                                    if (pluginData != null && pluginData.containsKey("error_code")) {
                                        log.error("Janus plugin error: {} - {}", 
                                            pluginData.get("error_code"), 
                                            pluginData.get("error"));
                                        janusResponse.setError((String) pluginData.get("error"));
                                        janusResponse.setErrorCode(((Number) pluginData.get("error_code")).intValue());
                                        break;
                                    }
                                }
                                
                                if (janusResponse.getJsep() != null) {
                                    log.info("Got subscriber event with JSEP offer - type: {}", janusResponse.getJsep().get("type"));
                                    break; // Found JSEP, stop polling
                                } else {
                                    log.warn("Subscriber event has no JSEP, will retry...");
                                }
                            } else if ("error".equals(janusType)) {
                                janusResponse = mapToJanusResponse(eventBody);
                                log.error("Janus error response: {}", janusResponse.getError());
                                break;
                            }
                        } else {
                            log.warn("Event body is null, will retry...");
                        }
                    } // end of for loop
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Interrupted while waiting for subscriber event: {}", e.getMessage());
                } catch (Exception e) {
                    log.warn("Failed to get subscriber event: {}", e.getMessage());
                }
            }
            
            if (janusResponse.getJsep() != null) {
                log.info("Configure subscriber successful - SDP offer received");
            } else {
                log.warn("Configure subscriber has no JSEP (SDP offer) - may need to wait longer");
            }
            
            return janusResponse;
        } catch (Exception e) {
            log.error("Error configuring subscriber: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to configure subscriber: " + e.getMessage());
        }
    }
    
    /**
     * Start subscriber (send SDP answer after receiving offer)
     */
    public JanusResponse startSubscriber(Long sessionId, Long handleId, String sdpAnswer) {
        log.info("Starting subscriber with SDP answer in session {} handle {}", sessionId, handleId);
        
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
            log.error("Error starting subscriber: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to start subscriber: " + e.getMessage());
        }
    }
    
    /**
     * List participants
     */
    public ParticipantListResponse listParticipants(Long sessionId, Long handleId, Long roomId) {
        log.info("Listing participants in room {}", roomId);
        
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
            log.error("Error listing participants: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to list participants: " + e.getMessage());
        }
    }
    
    /**
     * Destroy room
     */
    public JanusResponse destroyRoom(Long sessionId, Long handleId, Long roomId) {
        log.info("Destroying room {}", roomId);
        
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
            log.error("Error destroying room: {}", e.getMessage(), e);
            throw new InternalServerError("Failed to destroy room: " + e.getMessage());
        }
    }
    
    /**
     * Destroy session
     */
    public JanusResponse destroySession(Long sessionId) {
        log.info("Destroying session {}", sessionId);
        
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
            log.error("Error destroying session: {}", e.getMessage(), e);
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
        
        if (responseBody.containsKey("data")) {
            Object dataObj = responseBody.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                janusResponse.setData(data);
                
                if (data.containsKey("id")) {
                    janusResponse.setSessionId(((Number) data.get("id")).longValue());
                    janusResponse.setHandleId(((Number) data.get("id")).longValue());
                }
            }
        }
        
        if (responseBody.containsKey("session_id")) {
            janusResponse.setSessionId(((Number) responseBody.get("session_id")).longValue());
        }
        
        if (responseBody.containsKey("sender")) {
            janusResponse.setHandleId(((Number) responseBody.get("sender")).longValue());
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
        
        if (responseBody.containsKey("error")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
            janusResponse.setError((String) error.get("reason"));
            janusResponse.setErrorCode((Integer) error.get("code"));
        }
        
        return janusResponse;
    }
    
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}

