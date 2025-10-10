package com.example.backend.controller;

import com.example.backend.dto.model.ChapterPublicDto;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.model.CoursePublicDto;
import com.example.backend.dto.model.LessonPublicDto;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.dto.response.review.ReviewResponse;
import com.example.backend.service.ChapterService;
import com.example.backend.service.CourseService;
import com.example.backend.service.ReviewService;
import com.example.backend.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final CourseService courseService;
    private final ChapterService chapterService;
    private final ReviewService reviewService;
    private final LessonService lessonService;

    @GetMapping("/courses")
    public ResponseEntity<PaginationResponse<CourseDto>> getPublishedCourses(
            Pageable pageable,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) List<String> labels,
            @RequestParam(required = false) String search) {
        Page<CourseDto> courses = courseService.getPublishedCourses(pageable, tags, labels, search);
        return ResponseEntity.ok(new PaginationResponse<>(courses));
    }

    @GetMapping("/courses/{slug}")
    public ResponseEntity<CoursePublicDto> getCourseDetails(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getCourseBySlugPublic(slug));
    }

    @GetMapping("/courses/{slug}/chapters")
    public ResponseEntity<List<ChapterPublicDto>> getCourseChapters(@PathVariable String slug) {
        return ResponseEntity.ok(chapterService.getChaptersByCoursePublic(slug));
    }

    @GetMapping("/courses/{courseSlug}/reviews")
    public ResponseEntity<PaginationResponse<ReviewResponse>> getCourseReviews(
            @PathVariable String courseSlug,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getApprovedReviewsByCourseSlug(courseSlug, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(reviews));
    }

    @GetMapping("/lessons/{lessonSlug}")
    public ResponseEntity<LessonPublicDto> getLessonPublic(@PathVariable String lessonSlug) {
        return ResponseEntity.ok(lessonService.getLessonBySlugPublic(lessonSlug));
    }
}
