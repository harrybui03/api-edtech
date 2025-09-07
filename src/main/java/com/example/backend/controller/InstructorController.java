package com.example.backend.controller;

import com.example.backend.constant.CourseStatus;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.request.course.CourseRequest;
import com.example.backend.dto.request.instructor.InstructorIdRequest;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final CourseService courseService;

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
}
