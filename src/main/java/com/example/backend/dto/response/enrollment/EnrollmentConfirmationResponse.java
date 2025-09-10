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
public class EnrollmentConfirmationResponse {
    private String message;
    private UUID enrollmentId;
    private UUID courseId;
    private String courseTitle;
    private OffsetDateTime enrolledAt;
}
