package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.EntityType;
import com.example.backend.constant.UserRoleEnum;
import com.example.backend.dto.model.*;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.backend.util.SlugConverter.toSlug;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseMapper courseMapper;
    private final LabelService labelService;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final LabelRepository labelRepository;
    private final UserMapper userMapper;


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
        Course course = courseRepository.findBySlugAndStatusPublished(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Published course not found with slug: " + slug));
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

        List<Tag> tags = tagService.upsertTags(request.getTag().stream().map(TagDto::getName).collect(Collectors.toList()), savedCourse.getId(), EntityType.COURSE);
        List<Label> labels = labelService.upsertLabels(request.getLabel().stream().map(LabelDto::getName).collect(Collectors.toList()), savedCourse.getId(), EntityType.COURSE);

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

        List<Tag> tags = tagService.upsertTags(request.getTag().stream().map(TagDto::getName).collect(Collectors.toList()), course.getId(), EntityType.COURSE);
        List<Label> labels = labelService.upsertLabels(request.getLabel().stream().map(LabelDto::getName).collect(Collectors.toList()), course.getId(), EntityType.COURSE);

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
    public void upsertInstructors(UUID courseId, List<UUID> instructorIds) {
        if (instructorIds == null || instructorIds.isEmpty()) {
            throw new InvalidRequestDataException("Instructor list cannot be empty.");
        }

        Course course = findCourseById(courseId);
        checkCourseOwnership(course);

        Set<UUID> currentInstructorIds = course.getInstructors().stream()
                .map(ci -> ci.getUser().getId())
                .collect(Collectors.toSet());

        Set<UUID> newInstructorIdsSet = instructorIds.stream().collect(Collectors.toSet());

        // Determine which instructors to add
        List<UUID> idsToAdd = newInstructorIdsSet.stream()
                .filter(id -> !currentInstructorIds.contains(id))
                .collect(Collectors.toList());

        // Determine which instructors to remove
        List<UUID> idsToRemove = currentInstructorIds.stream()
                .filter(id -> !newInstructorIdsSet.contains(id))
                .collect(Collectors.toList());

        // Remove instructors
        if (!idsToRemove.isEmpty()) {
            courseInstructorRepository.deleteByCourseIdAndUserIdIn(courseId, idsToRemove);
        }

        // Add new instructors
        if (!idsToAdd.isEmpty()) {
            List<User> usersToAdd = userRepository.findAllById(idsToAdd);
            if (usersToAdd.size() != idsToAdd.size()) {
                throw new ResourceNotFoundException("One or more users to be added as instructors were not found.");
            }

            for (User user : usersToAdd) {
                if (user.getRoles().stream().noneMatch(role -> role.getRole() == UserRoleEnum.COURSE_CREATOR)) {
                    throw new ForbiddenException("User " + user.getEmail() + " must have the 'COURSE_CREATOR' role.");
                }
                CourseInstructor newInstructor = new CourseInstructor(course, user);
                courseInstructorRepository.save(newInstructor);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllInstructors() {
        List<User> instructors = userRepository.findByRole(UserRoleEnum.COURSE_CREATOR);
        return instructors.stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
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
