package com.example.backend.dto.response.rag;

import lombok.Data;

import java.util.List;

@Data
public class AskRAGResponse {
    private String answer;
    private List<CourseSource> sources;

    @Data
    public static class CourseSource {
        private String title;
        private String slug;
    }
}

