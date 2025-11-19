package com.example.backend.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordingProcessingMessage {
    private UUID jobId;
    private UUID liveSessionId;
    private String mjrObjectPath;  // MinIO path to raw .mjr file
    private Long roomId;
}

