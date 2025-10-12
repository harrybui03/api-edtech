package com.example.backend.dto.request.enrollment;

import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class EnrollmentRequest {
    @NotNull(message = "Course ID is required")
    private UUID courseId;
    
    private EnrollmentMemberType memberType;
    private EnrollmentRole role;
    private BigDecimal progress;
    private UUID currentLessonId;
}
