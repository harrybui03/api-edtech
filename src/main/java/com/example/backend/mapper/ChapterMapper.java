package com.example.backend.mapper;

import com.example.backend.dto.model.ChapterDto;
import com.example.backend.dto.model.ChapterPublicDto;
import com.example.backend.dto.request.course.ChapterRequest;
import com.example.backend.entity.Chapter;
import com.example.backend.entity.Lesson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChapterMapper {

    private final LessonMapper lessonMapper;

    public ChapterDto toDto(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        ChapterDto dto = new ChapterDto();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setSummary(chapter.getSummary());
        dto.setPosition(chapter.getPosition());
        dto.setSlug(chapter.getSlug());
        if (chapter.getLessons() != null) {
            List<Lesson> sortedLessons = chapter.getLessons().stream()
                    .sorted(Comparator.comparing(Lesson::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            dto.setLessons(sortedLessons.stream().map(lessonMapper::toDto).collect(Collectors.toList()));
        } else {
            dto.setLessons(Collections.emptyList());
        }
        return dto;
    }

    public ChapterPublicDto toPublicDto(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        ChapterPublicDto dto = new ChapterPublicDto();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setSlug(chapter.getSlug());
        dto.setPosition(chapter.getPosition());
        if (chapter.getLessons() != null) {
            List<Lesson> sortedLessons = chapter.getLessons().stream()
                    .sorted(Comparator.comparing(Lesson::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            dto.setLessons(sortedLessons.stream()
                    .map(lessonMapper::toPublicDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setLessons(Collections.emptyList());
        }
        return dto;
    }

    public Chapter toEntity(ChapterRequest request) {
        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setSummary(request.getSummary());
        return chapter;
    }

    public void updateEntityFromRequest(ChapterRequest request, Chapter chapter) {
        chapter.setTitle(request.getTitle());
        chapter.setSummary(request.getSummary());
    }
}