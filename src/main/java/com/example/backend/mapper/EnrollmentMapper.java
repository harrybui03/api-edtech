package com.example.backend.mapper;

import com.example.backend.dto.model.EnrollmentDto;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    
    public EnrollmentDto toDto(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(enrollment.getId());
        dto.setMemberId(enrollment.getMember() != null ? enrollment.getMember().getId() : null);
        dto.setCourseId(enrollment.getCourse() != null ? enrollment.getCourse().getId() : null);
        dto.setMemberType(enrollment.getMemberType());
        dto.setRole(enrollment.getRole());
        dto.setProgress(enrollment.getProgress());
        dto.setCurrentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null);
        dto.setCreation(enrollment.getCreation());
        dto.setModified(enrollment.getModified());
        return dto;
    }
    
    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .memberId(enrollment.getMember().getId())
                .memberName(enrollment.getMember().getFullName())
                .memberEmail(enrollment.getMember().getEmail())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .courseSlug(enrollment.getCourse().getSlug())
                .memberType(enrollment.getMemberType())
                .role(enrollment.getRole())
                .progress(enrollment.getProgress())
                .currentLessonId(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getId() : null)
                .currentLessonTitle(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getTitle() : null)
                .currentLessonSlug(enrollment.getCurrentLesson() != null ? enrollment.getCurrentLesson().getSlug() : null)
                .creation(enrollment.getCreation())
                .modified(enrollment.getModified())
                .build();
    }
}
