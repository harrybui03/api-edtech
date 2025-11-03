package com.example.backend.mapper;

import com.example.backend.dto.response.live.LiveSessionResponse;
import com.example.backend.entity.LiveSession;
import org.springframework.stereotype.Component;

@Component
public class LiveSessionMapper {
    
    public LiveSessionResponse toResponse(LiveSession liveSession) {
        if (liveSession == null) {
            return null;
        }
        
        return LiveSessionResponse.builder()
                .id(liveSession.getId())
                .janusSessionId(liveSession.getJanusSessionId())
                .janusHandleId(liveSession.getJanusHandleId())
                .roomId(liveSession.getRoomId())
                .instructorId(liveSession.getInstructor() != null ? liveSession.getInstructor().getId() : null)
                .instructorName(liveSession.getInstructor() != null ? liveSession.getInstructor().getFullName() : null)
                .batchId(liveSession.getBatch() != null ? liveSession.getBatch().getId() : null)
                .batchTitle(liveSession.getBatch() != null ? liveSession.getBatch().getTitle() : null)
                .status(liveSession.getStatus())
                .title(liveSession.getTitle())
                .description(liveSession.getDescription())
                .startedAt(liveSession.getStartedAt())
                .endedAt(liveSession.getEndedAt())
                .createdAt(liveSession.getCreatedAt())
                .updatedAt(liveSession.getUpdatedAt())
                .build();
    }
}

