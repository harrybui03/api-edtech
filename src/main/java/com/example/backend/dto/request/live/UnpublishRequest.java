package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to unpublish a stream (camera or screen)
 * Frontend provides sessionId and handleId directly
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnpublishRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Session ID is required")
    private Long sessionId;
    
    @NotNull(message = "Handle ID is required")
    private Long handleId;
}

