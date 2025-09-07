package com.example.backend.mapper;

import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.entity.Course;
import org.springframework.stereotype.Component;

public final class CourseMapper {

    public static CourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setShortIntroduction(course.getShortIntroduction());
        dto.setDescription(course.getDescription());
        dto.setImage(course.getImage());
        dto.setCategory(course.getCategory());
        dto.setStatus(course.getStatus());
        dto.setPublished(course.getPublished());
        dto.setPublishedOn(course.getPublishedOn());
        dto.setCoursePrice(course.getCoursePrice());
        dto.setEnrollments(course.getEnrollments());
        dto.setLessons(course.getLessons());
        dto.setRating(course.getRating());
        return dto;
    }

    public static Course toEntity(CourseRequest request) {
        if (request == null) {
            return null;
        }
        return Course.builder()
                .title(request.getTitle())
                .shortIntroduction(request.getShortIntroduction())
                .description(request.getDescription())
                .category(request.getCategory())
                .coursePrice(request.getCoursePrice())
                .build();
    }

    public static void updateEntityFromRequest(CourseRequest request, Course course) {
        if (request == null || course == null) {
            return;
        }
        course.setTitle(request.getTitle());
        course.setShortIntroduction(request.getShortIntroduction());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setCoursePrice(request.getCoursePrice());
    }
}
