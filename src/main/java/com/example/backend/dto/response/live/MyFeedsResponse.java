package com.example.backend.dto.response.live;

import com.example.backend.entity.ParticipantFeed;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyFeedsResponse {
    
    private Long roomId;
    
    private List<FeedInfo> feeds;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedInfo {
        private Long feedId;
        private ParticipantFeed.FeedType feedType;
        private String displayName;
        private Boolean isActive;
        private Long sessionId;
        private Long handleId;
    }
}

