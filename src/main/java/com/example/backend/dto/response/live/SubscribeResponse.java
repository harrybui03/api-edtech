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
public class SubscribeResponse {
    
    /**
     * SDP offer from Janus (subscriber cần tạo answer từ offer này)
     */
    private String sdpOffer;
    
    private String type; // "offer"
    
    private Long sessionId;
    
    private Long handleId;
    
    private Long feedId;
    
    private String error;
    
    private Integer errorCode;
}

