package com.example.backend.dto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDiscussionDto {
    private UUID id;
    private String title;
    private String content;
    private UserDTO author;
    private LocalDateTime createdAt;
    private List<BatchDocumentDto> documents;
}