package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.EntityType;
import com.example.backend.constant.UserRoleEnum;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.model.CoursePublicDto;
import com.example.backend.dto.model.LabelDto;
import com.example.backend.dto.model.TagDto;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.backend.util.SlugConverter.toSlug;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final TagRepository tagRepository;
    private final LabelRepository labelRepository;
    private final CourseMapper courseMapper;

    

    @Transactional(readOnly = true)
    public Page<CoursePublicDto> getPublishedCourses(Pageable pageable, List<String> tags, List<String> labels, String search) {
        Specification<Course> spec = CourseSpecification.isPublished();

        spec = spec.and(CourseSpecification.titleContains(search))
                .and(CourseSpecification.hasLabels(labels))
                .and(CourseSpecification.hasTags(tags));

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return getCoursePublicDtos(coursePage);
    }

    @Transactional(readOnly = true)
    public Page<CourseDto> getMyCourses(Pageable pageable, CourseStatus status) {
        User currentUser = getCurrentUser();
        Page<Course> coursePage = courseRepository.findCoursesByInstructorAndStatus(currentUser.getId(), status, pageable);
        return getCourseDtos(coursePage);
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with slug: " + slug));
        List<Tag> tags = tagRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
        List<Label> labels = labelRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
        return courseMapper.toDto(course, tags, labels);
    }

    @Transactional(readOnly = true)
    public CoursePublicDto getCourseBySlugPublic(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with slug: " + slug));
        List<Tag> tags = tagRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
        List<Label> labels = labelRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
        return courseMapper.toPublicDto(course, tags, labels);
    }

    @Transactional(readOnly = true)
    public CourseDto getCourseByIdForInstructor(UUID courseId) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);
        List<Tag> tags = tagRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
        List<Label> labels = labelRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
        return courseMapper.toDto(course, tags, labels);
    }

    @Transactional
    public CourseDto createCourse(CourseRequest request) {
        User currentUser = getCurrentUser();

        Course course = CourseMapper.toEntity(request);
        course.setStatus(CourseStatus.DRAFT);
        course.setSlug(generateUniqueSlug(request.getTitle()));

        Course savedCourse = courseRepository.save(course);

        CourseInstructor courseInstructor = new CourseInstructor();
        courseInstructor.setCourse(savedCourse);
        courseInstructor.setUser(currentUser);
        courseInstructorRepository.save(courseInstructor);

        List<Tag> tags = upsertTags(request.getTagDto().stream().map(TagDto::getName).collect(Collectors.toList()), savedCourse.getId());
        List<Label> labels = upsertLabels(request.getLabelDto().stream().map(LabelDto::getName).collect(Collectors.toList()), savedCourse.getId());

        return courseMapper.toDto(savedCourse, tags, labels);
    }

    @Transactional
    public CourseDto updateCourse(UUID courseId, CourseRequest request) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);

        courseMapper.updateEntityFromRequest(request, course);

        if (!course.getTitle().equals(request.getTitle())) {
            course.setSlug(generateUniqueSlug(request.getTitle()));
        }

        tagRepository.deleteByEntityIdAndEntityType(courseId, EntityType.COURSE);
        labelRepository.deleteByEntityIdAndEntityType(courseId, EntityType.COURSE);

        List<Tag> tags = upsertTags(request.getTagDto().stream().map(TagDto::getName).collect(Collectors.toList()), courseId);
        List<Label> labels = upsertLabels(request.getLabelDto().stream().map(LabelDto::getName).collect(Collectors.toList()), courseId);

        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toDto(updatedCourse, tags, labels);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = toSlug(title);
        String slug = baseSlug;
        int counter = 1;
        while (courseRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private List<Tag> upsertTags(List<String> tagNames, UUID courseId) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Tag> tags = tagNames.stream().map(name -> Tag.builder()
                .name(name)
                .entityId(courseId)
                .entityType(EntityType.COURSE)
                .build()).collect(Collectors.toList());
        return tagRepository.saveAll(tags);
    }

    private List<Label> upsertLabels(List<String> labelNames, UUID courseId) {
        if (labelNames == null || labelNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Label> labels = labelNames.stream().map(name -> Label.builder()
                .name(name)
                .entityId(courseId)
                .entityType(EntityType.COURSE)
                .build()).collect(Collectors.toList());
        return labelRepository.saveAll(labels);
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);
        if (course.getStatus().equals(CourseStatus.PUBLISHED)) {
            throw new ForbiddenException("Cannot delete a published course.");
        }

        tagRepository.deleteByEntityIdAndEntityType(courseId, EntityType.COURSE);
        labelRepository.deleteByEntityIdAndEntityType(courseId, EntityType.COURSE);
        courseRepository.delete(course);
    }

    @Transactional
    public void publishCourse(UUID courseId) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);

        if (course.getStatus().equals(CourseStatus.PUBLISHED)) {
            throw new InvalidRequestDataException("Course with id " + courseId + " is already published.");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        courseRepository.save(course);
    }

    @Transactional
    public void addInstructorToCourse(UUID courseId, UUID newInstructorId) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);

        User newInstructorUser = userRepository.findById(newInstructorId)
                .orElseThrow(() -> new ResourceNotFoundException("User to be added as instructor not found with id: " + newInstructorId));

        boolean isCourseCreator = newInstructorUser.getRoles().stream()
                .anyMatch(userRole -> userRole.getRole() == UserRoleEnum.COURSE_CREATOR);

        if (!isCourseCreator) {
            throw new ForbiddenException("User must have the 'COURSE_CREATOR' role to be added as an instructor.");
        }

        boolean alreadyExists = course.getInstructors().stream()
                .anyMatch(instructor -> instructor.getUser().getId().equals(newInstructorId));

        if (alreadyExists) {
            throw new InvalidRequestDataException("User is already an instructor for this course.");
        }

        CourseInstructor newCourseInstructor = new CourseInstructor();
        newCourseInstructor.setCourse(course);
        newCourseInstructor.setUser(newInstructorUser);
        courseInstructorRepository.save(newCourseInstructor);
    }

    @Transactional
    public void removeInstructorFromCourse(UUID courseId, UUID instructorIdToRemove) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);

        if (course.getInstructors().size() <= 1) {
            throw new ForbiddenException("Cannot remove the last instructor from a course.");
        }

        CourseInstructor instructorToRemove = course.getInstructors().stream()
                .filter(ci -> ci.getUser().getId().equals(instructorIdToRemove))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with id " + instructorIdToRemove + " not found on this course."));

        course.getInstructors().remove(instructorToRemove);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }

    private void checkCourseOwnership(Course course) {
        User currentUser = getCurrentUser();

        boolean isOwner = course.getInstructors().stream()
                .anyMatch(instructor -> instructor.getUser().getId().equals(currentUser.getId()));

        if (!isOwner) {
            throw new ForbiddenException("You are not an instructor for this course and cannot modify it.");
        }
    }

    private Course findCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    private Page<CourseDto> getCourseDtos(Page<Course> coursePage) {
        return coursePage.map(course -> {
            List<Tag> tags = tagRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
            List<Label> labels = labelRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
            return courseMapper.toDto(course, tags, labels);
        });
    }

    private Page<CoursePublicDto> getCoursePublicDtos(Page<Course> coursePage) {
        return coursePage.map(course -> {
            List<Tag> tags = tagRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
            List<Label> labels = labelRepository.findByEntityIdAndEntityType(course.getId(), EntityType.COURSE);
            return courseMapper.toPublicDto(course, tags, labels);
        });
    }
}
