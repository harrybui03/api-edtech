package com.example.backend.service;

import com.example.backend.dto.model.BatchDocumentDto;
import com.example.backend.dto.request.document.CreateBatchDocumentRequest;
import com.example.backend.entity.BatchDiscussion;
import com.example.backend.entity.BatchDocument;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.repository.BatchDiscussionRepository;
import com.example.backend.repository.BatchDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BatchDocumentService {

    private final BatchDocumentRepository batchDocumentRepository;
    private final BatchDiscussionRepository batchDiscussionRepository;

    @Transactional
    public BatchDocumentDto createBatchDocument(CreateBatchDocumentRequest request) {
        BatchDiscussion discussion = batchDiscussionRepository.findById(request.getBatchDiscussionId())
                .orElseThrow(() -> new ResourceNotFoundException("BatchDiscussion not found with id: " + request.getBatchDiscussionId()));

        BatchDocument document = new BatchDocument();
        document.setBatchDiscussion(discussion);
        document.setFileUrl(request.getFileUrl());
        document.setUploadedAt(LocalDateTime.now());

        BatchDocument savedDocument = batchDocumentRepository.save(document);

        return new BatchDocumentDto(
                savedDocument.getId(),
                savedDocument.getBatchDiscussion().getId(),
                savedDocument.getFileUrl(),
                savedDocument.getUploadedAt()
        );
    }
}