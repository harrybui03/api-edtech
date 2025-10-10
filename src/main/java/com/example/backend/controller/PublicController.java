package com.example.backend.controller;

import com.example.backend.dto.model.ChapterDto;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.dto.response.review.ReviewResponse;
import com.example.backend.service.ChapterService;
import com.example.backend.service.CourseService;
import com.example.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private final CourseService courseService;
    private final ChapterService chapterService;
    private final ReviewService reviewService;

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
    public ResponseEntity<CourseDto> getCourseDetails(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getCourseBySlug(slug));
    }

    @GetMapping("/courses/{slug}/chapters")
    public ResponseEntity<List<ChapterDto>> getCourseChapters(@PathVariable String slug) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(slug));
    }

    @GetMapping("/courses/{courseSlug}/reviews")
    public ResponseEntity<PaginationResponse<ReviewResponse>> getCourseReviews(
            @PathVariable String courseSlug,
            Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getApprovedReviewsByCourseSlug(courseSlug, pageable);
        return ResponseEntity.ok(new PaginationResponse<>(reviews));
    }
}
