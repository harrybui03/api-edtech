package com.example.backend.dto.request.rag;

import lombok.Data;

import java.util.List;

@Data
public class AskRAGRequest {
    private String question;
    private String userId;
    private String lessonId;
    private List<ChatMessage> chatHistory;

    @Data
    public static class ChatMessage {
        private String question;
        private String answer;
    }
}

