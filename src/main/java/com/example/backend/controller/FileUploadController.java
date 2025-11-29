package com.example.backend.controller;

import com.example.backend.dto.request.upload.PresignedUrlRequest;
import com.example.backend.dto.request.upload.TranscodeRequest;
import com.example.backend.dto.response.upload.PresignedUrlResponse;
import com.example.backend.entity.Job;
import com.example.backend.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/generate-url")
    @Operation(summary = "Generate Presigned Upload URL", description = "Generates a presigned URL to upload a file to storage.")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(@RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(fileUploadService.generatePresignedUploadUrl(request.getFileName(), request.getEntityId(), request.getPurpose()));
    }

    @GetMapping("/get-url")
    @Operation(summary = "Generate Presigned GET URL", description = "Generates a presigned URL to view/download a file from storage.")
    public ResponseEntity<PresignedUrlResponse> generatePresignedGetUrl(@RequestParam String objectName) {
        String url = fileUploadService.generatePresignedGetUrl(objectName);
        return ResponseEntity.ok(new PresignedUrlResponse(url, objectName));
    }


    @PostMapping("/videos")
    public ResponseEntity<Job> uploadVideoForTranscoding(@ModelAttribute TranscodeRequest transcodeRequest) {
        fileUploadService.uploadAndQueueForTranscoding(transcodeRequest);
        return ResponseEntity.accepted().build();
    }

}