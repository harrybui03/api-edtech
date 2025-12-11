package com.example.backend.dto.response.live;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JanusResponse {
    
    private String janus;
    
    private String transaction;
    
    private Long sessionId;
    
    private Long handleId;
    
    private UUID liveSessionId;
    
    private Map<String, Object> plugindata;
    
    private Map<String, Object> jsep;
    
    private Map<String, Object> data;
    
    private String error;
    
    private Integer errorCode;
}

