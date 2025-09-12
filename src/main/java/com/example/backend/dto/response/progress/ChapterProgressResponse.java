package com.example.backend.dto.response.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressResponse {
    private UUID chapterId;
    private String chapterTitle;
    private List<LessonProgressResponse> lessons;
}
