package com.example.backend.dto.request.progress;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoWatchTimeRequest {
    @NotBlank(message = "Watch time is required")
    private String watchTime;
    
    @NotBlank(message = "Source is required")
    private String source;
}
