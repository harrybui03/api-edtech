package com.example.backend.controller;

import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final CourseService courseService;

    @GetMapping("/courses")
    public ResponseEntity<PaginationResponse<CourseDto>> getPublishedCourses(
            Pageable pageable,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        Page<CourseDto> courses = courseService.getPublishedCourses(pageable, category, search);
        return ResponseEntity.ok(new PaginationResponse<>(courses));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDto> getCourseDetails(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourseDetails(courseId));
    }
}
