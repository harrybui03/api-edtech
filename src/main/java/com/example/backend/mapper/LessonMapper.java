package com.example.backend.mapper;

import com.example.backend.dto.model.LessonDto;
import com.example.backend.dto.model.LessonPublicDto;
import com.example.backend.dto.request.course.LessonRequest;
import com.example.backend.entity.Lesson;
import com.example.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class LessonMapper {

    private final FileUploadService fileUploadService;

    public LessonDto toDto(Lesson lesson) {
        if (lesson == null) {
            return null;
        }
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setSlug(lesson.getSlug());
        dto.setContent(lesson.getContent());
        dto.setQuizDto(QuizMapper.toDto(lesson.getQuiz()));
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setFileUrl(lesson.getFileUrl());

        dto.setPosition(lesson.getPosition());
        return dto;
    }

    public LessonPublicDto toPublicDto(Lesson lesson) {
        if (lesson == null) {
            return null;
        }
        LessonPublicDto dto = new LessonPublicDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setSlug(lesson.getSlug());
        dto.setPosition(lesson.getPosition());
        return dto;
    }

    public Lesson toEntity(LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setFileUrl(request.getFileUrl());
        return lesson;
    }

    public Lesson updateEntityFromRequest(LessonRequest request, Lesson lesson) {
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setFileUrl(request.getFileUrl());

        return lesson;
    }
}