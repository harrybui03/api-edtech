package com.example.backend.dto.request.live;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRecordingRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Total chunks is required")
    @Min(value = 1, message = "Must have at least 1 chunk")
    private Integer totalChunks;
    
    private Integer totalDurationSeconds;
}

