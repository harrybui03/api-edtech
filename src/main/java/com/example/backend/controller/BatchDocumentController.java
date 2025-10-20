package com.example.backend.controller;

import com.example.backend.dto.model.BatchDocumentDto;
import com.example.backend.dto.request.document.CreateBatchDocumentRequest;
import com.example.backend.service.BatchDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch-documents")
@RequiredArgsConstructor
public class BatchDocumentController {

    private final BatchDocumentService batchDocumentService;

    @PostMapping
    @Operation(summary = "Create a batch document record", description = "Associates an uploaded file with a batch discussion.")
    public ResponseEntity<BatchDocumentDto> createBatchDocument(@Valid @RequestBody CreateBatchDocumentRequest request) {
        BatchDocumentDto createdDocument = batchDocumentService.createBatchDocument(request);
        return new ResponseEntity<>(createdDocument, HttpStatus.CREATED);
    }
}