package com.example.backend.dto.response.live;

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
public class ParticipantListResponse {
    
    private Long roomId;
    
    private List<Participant> participants;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participant {
        private Long id;
        private String display;
        private Boolean publisher;
    }
}

