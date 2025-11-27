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
    
    // Note: sessionId and handleId are now created by backend
    // Each feed subscription gets its own session/handle per Janus VideoRoom design
}
