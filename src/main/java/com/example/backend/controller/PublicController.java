package com.example.backend.controller;

import com.example.backend.dto.model.ChapterDto;
import com.example.backend.dto.model.CourseDto;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.service.ChapterService;
import com.example.backend.service.CourseService;
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

    @GetMapping("/courses/{courseId}/chapters")
    public ResponseEntity<List<ChapterDto>> getCourseChapters(@PathVariable UUID courseId) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }
}
