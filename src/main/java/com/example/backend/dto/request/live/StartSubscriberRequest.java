package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartSubscriberRequest {
    
    @NotNull(message = "Session ID is required")
    private Long sessionId;
    
    @NotNull(message = "Handle ID is required")
    private Long handleId;
    
    @NotNull(message = "SDP answer is required")
    private String sdpAnswer;
}

