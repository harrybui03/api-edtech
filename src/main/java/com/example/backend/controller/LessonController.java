package com.example.backend.controller;

import com.example.backend.dto.model.LessonDto;
import com.example.backend.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/{slug}")
    public ResponseEntity<LessonDto> getLessonBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(lessonService.getLessonBySlug(slug));
    }
}