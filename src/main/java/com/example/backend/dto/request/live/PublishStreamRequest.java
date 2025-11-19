package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishStreamRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "SDP offer is required")
    private String sdp;
    
    /**
     * Stream type: "camera", "screen", or "camera-screen" (both)
     * Optional - defaults to "camera"
     */
    private String streamType;
    
    // sessionId and handleId are auto-detected by backend from database
    // No need for frontend to send them!
}

