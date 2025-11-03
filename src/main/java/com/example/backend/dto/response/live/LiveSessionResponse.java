package com.example.backend.dto.response.live;

import com.example.backend.entity.LiveSession;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiveSessionResponse {
    
    private UUID id;
    
    private Long janusSessionId;
    
    private Long janusHandleId;
    
    private Long roomId;
    
    private UUID instructorId;
    
    private String instructorName;
    
    private UUID batchId;
    
    private String batchTitle;
    
    private LiveSession.LiveStatus status;
    
    private String title;
    
    private String description;
    
    private OffsetDateTime startedAt;
    
    private OffsetDateTime endedAt;
    
    private OffsetDateTime createdAt;
    
    private OffsetDateTime updatedAt;
}

