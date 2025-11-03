package com.example.backend.dto.response.live;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublishStreamResponse {
    
    private String sdpAnswer;
    
    private String type; // "answer"
    
    private Long sessionId;
    
    private Long handleId;
    
    private String error;
    
    private Integer errorCode;
}

