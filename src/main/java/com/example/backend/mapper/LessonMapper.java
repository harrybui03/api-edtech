package com.example.backend.mapper;

import com.example.backend.dto.model.LessonDto;
import com.example.backend.dto.request.course.LessonRequest;
import com.example.backend.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {

    public LessonDto toDto(Lesson lesson) {
        if (lesson == null) {
            return null;
        }
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setSlug(lesson.getSlug());
        dto.setContent(lesson.getContent());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setFileUrl(lesson.getFileUrl());
        dto.setDuration(lesson.getDuration());
        dto.setPosition(lesson.getPosition());
        return dto;
    }

    public Lesson toEntity(LessonRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setFileUrl(request.getFileUrl());
        lesson.setDuration(request.getDuration());
        return lesson;
    }

    public void updateEntityFromRequest(LessonRequest request, Lesson lesson) {
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setFileUrl(request.getFileUrl());
        lesson.setDuration(request.getDuration());
    }
}