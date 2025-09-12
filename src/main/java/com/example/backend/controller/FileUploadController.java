package com.example.backend.controller;

import com.example.backend.dto.request.upload.PresignedUrlRequest;
import com.example.backend.dto.response.upload.PresignedUrlResponse;
import com.example.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/generate-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(@RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(fileUploadService.generatePresignedUploadUrl(request.getFileName(), request.getEntityId(), request.getPurpose()));
    }
}