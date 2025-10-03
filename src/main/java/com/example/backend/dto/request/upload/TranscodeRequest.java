package com.example.backend.dto.request.upload;

import com.example.backend.constant.UploadPurpose;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class TranscodeRequest {
    MultipartFile file;
    UUID entityId;
    UploadPurpose purpose;
}
