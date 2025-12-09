package com.example.backend.dto.response.live;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRecordingsResponse {
    private UUID batchId;
    private String batchTitle;
    private String batchSlug;
    private List<RecordingInfo> recordings;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordingInfo {
        private UUID sessionId;
        private Long roomId;
        private String title;
        private String description;
        private String objectName;
        private Integer durationSeconds;
        private OffsetDateTime recordedAt;
        private String instructorName;
    }
}

