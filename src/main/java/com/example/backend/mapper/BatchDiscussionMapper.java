package com.example.backend.mapper;

import com.example.backend.dto.model.BatchDiscussionDto;
import com.example.backend.dto.model.UserDTO;
import com.example.backend.dto.model.BatchDocumentDto;
import com.example.backend.entity.BatchDiscussion;
import com.example.backend.entity.BatchDocument;
import com.example.backend.repository.BatchDocumentRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BatchDiscussionMapper {

    public BatchDiscussionDto toDto(BatchDiscussion discussion, List<BatchDocument> documents) {
        if (discussion == null) {
            return null;
        }

        UserDTO authorDto = new UserDTO();
        authorDto.setId(discussion.getUser().getId());
        authorDto.setFullName(discussion.getUser().getFullName());
        authorDto.setUserImage(discussion.getUser().getUserImage());
        
        // Fetch and map documents
        List<BatchDocumentDto> documentDtos = documents.stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());

        return new BatchDiscussionDto(discussion.getId(), discussion.getTitle(), discussion.getContent(), authorDto, discussion.getCreatedAt(), documentDtos);
    }

    // Helper method to map BatchDocument to BatchDocumentDto
    private BatchDocumentDto toDocumentDto(BatchDocument document) {
        if (document == null) {
            return null;
        }
        return new BatchDocumentDto(document.getId(), document.getBatchDiscussion().getId(), document.getFileUrl(), document.getUploadedAt());
    }
}