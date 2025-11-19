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
        
        // Enable recording
        body.put("record", true);
        body.put("rec_dir", "/tmp/janus-recordings");
        
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
                // Try to get event (with retry and longer timeout)
                try {
                    // Retry up to 10 times with 300ms between each (total ~3 seconds)
                    for (int attempt = 0; attempt < 10; attempt++) {
                        Thread.sleep(300); // Wait 300ms for Janus to process
                        
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
                        
                        if ("event".equals(janusType)) {
                            // Check if this is a relevant publish event (has JSEP or configured)
                            boolean hasJsep = eventBody.containsKey("jsep");
                            boolean isPublishEvent = false;
                            
                            if (eventBody.containsKey("plugindata")) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> plugindata = (Map<String, Object>) eventBody.get("plugindata");
                                if (plugindata != null && plugindata.containsKey("data")) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> data = (Map<String, Object>) plugindata.get("data");
                                    if (data != null) {
                                        // Check if this is a publish success event
                                        isPublishEvent = data.containsKey("configured") || 
                                                       data.containsKey("publishers") ||
                                                       (data.containsKey("videoroom") && 
                                                        "event".equals(data.get("videoroom")) && 
                                                        !data.containsKey("unpublished") && 
                                                        !data.containsKey("leaving"));
                                    }
                                }
                            }
                            
                            // Only process if it's a publish event with JSEP or has error
                            if (hasJsep || isPublishEvent) {
                                janusResponse = mapToJanusResponse(eventBody);
                                
                                // Check if event contains error
                                if (janusResponse.getPlugindata() != null) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> pluginData = (Map<String, Object>) janusResponse.getPlugindata().get("data");
                                    if (pluginData != null && pluginData.containsKey("error_code")) {
                                        janusResponse.setError((String) pluginData.get("error"));
                                        janusResponse.setErrorCode(((Number) pluginData.get("error_code")).intValue());
                                        break; // Error found, stop polling
                                    }
                                }
                                
                                if (janusResponse.getJsep() != null) {
                                    break; // Found JSEP, stop polling
                                }
                            }
                        } else if ("error".equals(janusType)) {
                            janusResponse = mapToJanusResponse(eventBody);
                            break; // Error, stop polling
                        }
                    }
                    } // end of for loop
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // Failed to get event
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
                    // Retry up to 3 times with 500ms between each
                    for (int attempt = 0; attempt < 3; attempt++) {
                        Thread.sleep(500);
                        
                        @SuppressWarnings("rawtypes")
                        ResponseEntity<Map> eventResponse = restTemplate.getForEntity(
                            janusServerUrl + "/" + sessionId + "?maxev=1",
                            Map.class
                        );
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eventBody = eventResponse.getBody();
                    
                        if (eventBody != null) {
                            String janusType = (String) eventBody.get("janus");
                            
                            if ("event".equals(janusType)) {
                                janusResponse = mapToJanusResponse(eventBody);
                                
                                // Check if event contains error
                                if (janusResponse.getPlugindata() != null) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> pluginData = (Map<String, Object>) janusResponse.getPlugindata().get("data");
                                    if (pluginData != null && pluginData.containsKey("error_code")) {
                                        janusResponse.setError((String) pluginData.get("error"));
                                        janusResponse.setErrorCode(((Number) pluginData.get("error_code")).intValue());
                                        break;
                                    }
                                }
                                
                                if (janusResponse.getJsep() != null) {
                                    break; // Found JSEP, stop polling
                                }
                            } else if ("error".equals(janusType)) {
                                janusResponse = mapToJanusResponse(eventBody);
                                break;
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

