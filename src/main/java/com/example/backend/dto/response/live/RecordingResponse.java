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
public class RecordingResponse {
    
    private Long roomId;
    private LiveSession.RecordingStatus status;
    private String message;
    private String recordingUrl;
    private Integer duration;
}

