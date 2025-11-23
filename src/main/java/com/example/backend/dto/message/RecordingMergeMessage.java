package com.example.backend.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingMergeMessage {
    
    private UUID jobId;
    private UUID liveSessionId;
}

