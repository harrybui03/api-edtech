package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinLiveRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Participant type is required (publisher/subscriber)")
    private String ptype;
    
    // Optional: Display name to show in the room (if not provided, use user's full name)
    private String displayName;
}

