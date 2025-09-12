package com.example.backend.dto.response.upload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignedUrlResponse {
    private String uploadUrl;
    private String objectName;
}