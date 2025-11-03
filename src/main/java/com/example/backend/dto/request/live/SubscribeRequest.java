package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Publisher feed ID is required")
    private Long feedId;
    
    /**
     * Subscriber session ID (from join response)
     */
    @NotNull(message = "Session ID is required")
    private Long sessionId;
    
    /**
     * Subscriber handle ID (from join response)
     */
    @NotNull(message = "Handle ID is required")
    private Long handleId;
}

