package com.example.backend.dto.response.enrollment;

import com.example.backend.constant.EnrollmentMemberType;
import com.example.backend.constant.EnrollmentRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private String memberEmail;
    private UUID courseId;
    private String courseTitle;
    private String courseSlug;
    private EnrollmentMemberType memberType;
    private EnrollmentRole role;
    private BigDecimal progress;
    private UUID currentLessonId;
    private String currentLessonTitle;
    private String currentLessonSlug;
    private OffsetDateTime creation;
    private OffsetDateTime modified;
}
