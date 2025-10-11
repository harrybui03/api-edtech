package com.example.backend.dto.model;

import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EnrollmentDto {
    private UUID id;
    private UUID memberId;
    private UUID courseId;
    private EnrollmentMemberType memberType;
    private EnrollmentRole role;
    private BigDecimal progress;
    private UUID currentLessonId;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
}
