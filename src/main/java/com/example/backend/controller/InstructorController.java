package com.example.backend.controller;

import com.example.backend.constant.CourseStatus;
import com.example.backend.dto.model.ChapterDto;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.model.LessonDto;
import com.example.backend.dto.request.course.ChapterRequest;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.dto.request.course.LessonRequest;
import com.example.backend.dto.request.instructor.InstructorIdRequest;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.service.ChapterService;
import com.example.backend.service.CourseService;
import com.example.backend.service.LessonService;
import com.example.backend.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final CourseService courseService;
    private final ChapterService chapterService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;

    @PostMapping("/courses")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseRequest request) {
        return new ResponseEntity<>(courseService.createCourse(request), HttpStatus.CREATED);
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable UUID courseId, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<PaginationResponse<CourseDto>> getMyCourses(Pageable pageable, @RequestParam(required = false) CourseStatus status) {
        Page<CourseDto> courses = courseService.getMyCourses(pageable, status);
        return ResponseEntity.ok(new PaginationResponse<>(courses));
    }

    @PutMapping("/courses/{courseId}/publish")
    public ResponseEntity<Void> publishCourse(@PathVariable UUID courseId) {
        courseService.publishCourse(courseId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/courses/{courseId}/instructors")
    public ResponseEntity<Void> addInstructorToCourse(@PathVariable UUID courseId, @RequestBody InstructorIdRequest request) {
        courseService.addInstructorToCourse(courseId, request.getInstructorId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/courses/{courseId}/instructors/{instructorId}")
    public ResponseEntity<Void> removeInstructorFromCourse(@PathVariable UUID courseId, @PathVariable UUID instructorId) {
        courseService.removeInstructorFromCourse(courseId, instructorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/courses/{courseId}/chapters")
    public ResponseEntity<ChapterDto> createChapter(@PathVariable UUID courseId, @RequestBody ChapterRequest request) {
        return new ResponseEntity<>(chapterService.createChapter(courseId, request), HttpStatus.CREATED);
    }

    @PutMapping("/chapters/{chapterId}")
    public ResponseEntity<ChapterDto> updateChapter(@PathVariable UUID chapterId, @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Void> deleteChapter(@PathVariable UUID chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chapters/{chapterId}/lessons")
    public ResponseEntity<LessonDto> createLesson(@PathVariable UUID chapterId, @RequestBody LessonRequest request) {
        return new ResponseEntity<>(lessonService.createLesson(chapterId, request), HttpStatus.CREATED);
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable UUID lessonId, @RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/enrollments")
    @Operation(summary = "Get course enrollments", description = "Instructor gets all enrollments for their course")
    public ResponseEntity<List<EnrollmentResponse>> getCourseEnrollments(@PathVariable UUID courseId) {
        String currentUserEmail = getCurrentUserEmail();
        List<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId, currentUserEmail);
        return ResponseEntity.ok(enrollments);
    }
    
    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Remove enrollment", description = "Instructor removes a student from their course")
    public ResponseEntity<Void> removeEnrollment(@PathVariable UUID enrollmentId) {
        String currentUserEmail = getCurrentUserEmail();
        enrollmentService.removeEnrollment(enrollmentId, currentUserEmail);
        return ResponseEntity.noContent().build();
    }
    
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
