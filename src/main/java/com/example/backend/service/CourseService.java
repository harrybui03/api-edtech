package com.example.backend.service;

import com.example.backend.constant.CourseStatus;
import com.example.backend.constant.UserRoleEnum;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.entity.Course;
import com.example.backend.entity.CourseInstructor;
import com.example.backend.entity.User;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.CourseInstructorRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseInstructorRepository courseInstructorRepository;

//    @Transactional(readOnly = true)
//    public Page<CourseDto> getPublishedCourses(Pageable pageable, String category, String search) {
//        return courseRepository.findPublishedCourses(category, search, pageable)
//                .map(CourseMapper::toDto);
//    }

//    @Transactional(readOnly = true)
//    public Page<CourseDto> getMyCourses(Pageable pageable, CourseStatus status) {
//        User currentUser = getCurrentUser();
//        return courseRepository.findCoursesByInstructorAndStatus(currentUser.getId(), status, pageable)
//                .map(CourseMapper::toDto);
//    }

    @Transactional(readOnly = true)
    public CourseDto getCourseDetails(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return CourseMapper.toDto(course);
    }

    @Transactional
    public CourseDto createCourse(CourseRequest request) {
        User currentUser = getCurrentUser();

        Course course = CourseMapper.toEntity(request);
        course.setStatus(CourseStatus.DRAFT);
        course.setPublished(false);

        Course savedCourse = courseRepository.save(course);

        CourseInstructor courseInstructor = new CourseInstructor();
        courseInstructor.setCourse(savedCourse);
        courseInstructor.setUser(currentUser);
        courseInstructorRepository.save(courseInstructor);

        return CourseMapper.toDto(savedCourse);
    }

    @Transactional
    public CourseDto updateCourse(UUID courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        checkCourseOwnership(course);

        CourseMapper.updateEntityFromRequest(request, course);

        Course updatedCourse = courseRepository.save(course);
        return CourseMapper.toDto(updatedCourse);
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        checkCourseOwnership(course);
        if (course.getPublished()){
            throw new ForbiddenException("Cannot delete a published course.");
        }

        courseRepository.delete(course);
    }

    @Transactional
    public void publishCourse(UUID courseId) {
        Course course = findCourseById(courseId);
        checkCourseOwnership(course);

        if (Boolean.TRUE.equals(course.getPublished())) {
            throw new InvalidRequestDataException("Course with id " + courseId + " is already published.");
        }

        course.setPublished(true);
        course.setPublishedOn(LocalDate.now());
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
}
