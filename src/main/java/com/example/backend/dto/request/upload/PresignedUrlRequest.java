package com.example.backend.dto.request.upload;

import com.example.backend.constant.UploadPurpose;
import lombok.Data;

import java.util.UUID;

@Data
public class PresignedUrlRequest {
    private String fileName;
    private UUID entityId;
    private UploadPurpose purpose;
}