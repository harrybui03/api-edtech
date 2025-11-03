package com.example.backend.dto.request.live;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnpublishScreenRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Screen session ID is required")
    private Long screenSessionId;
    
    @NotNull(message = "Screen handle ID is required")
    private Long screenHandleId;
}

