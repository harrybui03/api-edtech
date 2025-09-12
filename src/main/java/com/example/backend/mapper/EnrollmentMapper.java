package com.example.backend.mapper;

import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    
    public EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .memberId(enrollment.getMember().getId())
                .memberName(enrollment.getMember().getFullName())
                .memberEmail(enrollment.getMember().getEmail())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .memberType(enrollment.getMemberType())
                .role(enrollment.getRole())
                .progress(enrollment.getProgress())
                .currentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null)
                .currentLessonTitle(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getTitle() : null)
                .enrolledAt(enrollment.getCreation())
                .build();
    }
}
