package com.example.backend.dto.response.enrollment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class CurrentEnrollmentResponse {
    private String courseTitle;
    private BigDecimal price;
    private OffsetDateTime enrollmentDate;
}
