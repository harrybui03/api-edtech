package com.example.backend.dto.response.live;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolledBatchResponse {
    private UUID id;
    private String slug;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}



