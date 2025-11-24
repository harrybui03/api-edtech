package com.example.backend.mapper;

import com.example.backend.dto.model.*;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.entity.Chapter;
import com.example.backend.entity.Course;
import com.example.backend.entity.Label;
import com.example.backend.entity.Tag;
import com.example.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.backend.mapper.LabelMapper.toLabelDtoList;
import static com.example.backend.mapper.TagMapper.toTagDtoList;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final ChapterMapper chapterMapper;
    private final UserMapper userMapper;

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
        dto.setImage(course.getImage());
        dto.setVideoLink(course.getVideoLink());
        dto.setStatus(course.getStatus());
        dto.setPaidCourse(course.getPaidCourse());
        dto.setCoursePrice(course.getCoursePrice());
        dto.setSellingPrice(course.getSellingPrice());
        dto.setCurrency(course.getCurrency());
        dto.setAmountUsd(course.getAmountUsd());
        dto.setEnrollments(course.getEnrollments());
        dto.setLessons(course.getLessons());
        dto.setRating(course.getRating());
        dto.setLanguage(course.getLanguage());
        dto.setTags(toTagDtoList(tags));
        dto.setLabels(toLabelDtoList(labels));
        dto.setTargetAudience(course.getTargetAudience());
        dto.setSkillLevel(course.getSkillLevel());
        dto.setLearnerProfileDesc(course.getLearnerProfileDesc());
        if (course.getChapters() != null) {
            List<ChapterDto> chapterDtos = course.getChapters().stream()
                    .sorted(Comparator.comparing(Chapter::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(chapterMapper::toDto)
                    .collect(Collectors.toList());
            dto.setChapters(chapterDtos);
        }
        if (course.getInstructors() != null) {
            List<UserDTO> userDTOS = course.getInstructors().stream().map(courseInstructor -> userMapper.toUserDTO(courseInstructor.getUser())).collect(Collectors.toList());
            dto.setInstructors(userDTOS);
        }
        return dto;
    }

    public CourseDto toDto(Course course) {
        return toDto(course, Collections.emptyList(), Collections.emptyList());
    }

    public CoursePublicDto toPublicDto(Course course, List<Tag> tags, List<Label> labels) {
        if (course == null) {
            return null;
        }
        CoursePublicDto dto = new CoursePublicDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setSlug(course.getSlug());
        dto.setShortIntroduction(course.getShortIntroduction());
        dto.setDescription(course.getDescription());
        dto.setImage(course.getImage());
        dto.setStatus(course.getStatus());
        dto.setSellingPrice(course.getSellingPrice());
        dto.setCurrency(course.getCurrency());
        dto.setEnrollments(course.getEnrollments());
        dto.setLessons(course.getLessons());
        dto.setRating(course.getRating() != null ? course.getRating().doubleValue() : null);
        dto.setLanguage(course.getLanguage());
        dto.setTags(toTagDtoList(tags));
        dto.setLabels(toLabelDtoList(labels));
        dto.setTargetAudience(course.getTargetAudience());
        dto.setSkillLevel(course.getSkillLevel());
        dto.setLearnerProfileDesc(course.getLearnerProfileDesc());
        dto.setPaidCourse(course.getPaidCourse());
        dto.setVideoLink(course.getVideoLink());
        
        // Map instructors
        if (course.getInstructors() != null) {
            dto.setInstructors(course.getInstructors().stream()
                    .map(ci -> new InstructorDto(
                            ci.getUser().getId(),
                            ci.getUser().getFullName(),
                            ci.getUser().getEmail(),
                            ci.getUser().getUserImage()
                    ))
                    .collect(Collectors.toList()));
        }
        
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
                .image(request.getImage())
                .videoLink(request.getVideoLink())
                .paidCourse(request.getPaidCourse())
                .coursePrice(request.getCoursePrice())
                .sellingPrice(request.getSellingPrice())
                .currency(request.getCurrency())
                .amountUsd(request.getAmountUsd())
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
        course.setLanguage(request.getLanguage());
        course.setTargetAudience(request.getTargetAudience());
        course.setSkillLevel(request.getSkillLevel());
        course.setLearnerProfileDesc(request.getLearnerProfileDesc());
    }
}
