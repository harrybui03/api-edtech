package com.example.backend.dto.response.enrollment;

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
public class BatchEnrollmentResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private OffsetDateTime enrolledAt;
}


