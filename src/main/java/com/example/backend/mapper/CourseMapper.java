package com.example.backend.mapper;

import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.model.LabelDto;
import com.example.backend.dto.model.TagDto;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.entity.Course;
import com.example.backend.entity.Label;
import com.example.backend.entity.Tag;
import com.example.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final FileUploadService fileUploadService;

    public  CourseDto toDto(Course course, List<Tag> tags, List<Label> labels) {
        if (course == null) {
            return null;
        }
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setSlug(course.getSlug());
        dto.setShortIntroduction(course.getShortIntroduction());
        dto.setDescription(course.getDescription());

        if (StringUtils.hasText(course.getImage())) {
            dto.setImage(fileUploadService.generatePresignedGetUrl(course.getImage()));
        }

        dto.setVideoLink(course.getVideoLink());
        dto.setStatus(course.getStatus());
        dto.setPublished(course.getPublished());
        dto.setPublishedOn(course.getPublishedOn());
        dto.setCoursePrice(course.getCoursePrice());
        dto.setSellingPrice(course.getSellingPrice());
        dto.setCurrency(course.getCurrency());
        dto.setAmountUsd(course.getAmountUsd());
        dto.setEnableCertification(course.getEnableCertification());
        dto.setEnrollments(course.getEnrollments());
        dto.setLessons(course.getLessons());
        dto.setRating(course.getRating());
        dto.setLanguage(course.getLanguage());
        dto.setTags(toTagDtoList(tags));
        dto.setLabels(toLabelDtoList(labels));
        return dto;
    }

    public CourseDto toDto(Course course) {
        return toDto(course, Collections.emptyList(), Collections.emptyList());
    }

    private List<TagDto> toTagDtoList(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags.stream()
                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    private List<LabelDto> toLabelDtoList(List<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        }
        return labels.stream()
                .map(label -> new LabelDto(label.getId(), label.getName()))
                .collect(Collectors.toList());
    }

    public static Course toEntity(CourseRequest request) {
        if (request == null) {
            return null;
        }
        return Course.builder()
                .title(request.getTitle())
                .shortIntroduction(request.getShortIntroduction())
                .description(request.getDescription())
                .image(request.getImage())
                .videoLink(request.getVideoLink())
                .paidCourse(request.getPaidCourse())
                .coursePrice(request.getCoursePrice())
                .sellingPrice(request.getSellingPrice())
                .currency(request.getCurrency())
                .amountUsd(request.getAmountUsd())
                .enableCertification(request.getEnableCertification())
                .language(request.getLanguage())
                .build();
    }

    public void updateEntityFromRequest(CourseRequest request, Course course) {
        if (request == null || course == null) {
            return;
        }
        course.setTitle(request.getTitle());
        course.setShortIntroduction(request.getShortIntroduction());
        course.setDescription(request.getDescription());
        course.setImage(request.getImage());
        course.setVideoLink(request.getVideoLink());
        course.setPaidCourse(request.getPaidCourse());
        course.setCoursePrice(request.getCoursePrice());
        course.setSellingPrice(request.getSellingPrice());
        course.setCurrency(request.getCurrency());
        course.setAmountUsd(request.getAmountUsd());
        course.setEnableCertification(request.getEnableCertification());
        course.setLanguage(request.getLanguage());
    }
}
