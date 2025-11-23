package com.example.backend.dto.response.live;

import com.example.backend.entity.LiveSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordingStatusResponse {
    
    private Long roomId;
    private LiveSession.RecordingStatus status;
    private String message;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer totalChunks;
}

