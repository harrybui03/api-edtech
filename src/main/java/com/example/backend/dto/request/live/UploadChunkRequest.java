package com.example.backend.dto.request.live;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadChunkRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Chunk index is required")
    @Min(value = 0, message = "Chunk index must be >= 0")
    private Integer chunkIndex;
    
    private Integer durationSeconds;
}

