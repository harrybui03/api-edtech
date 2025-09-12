package com.example.backend.service;

import com.example.backend.constant.UploadPurpose;
import com.example.backend.dto.response.upload.PresignedUrlResponse;
import com.example.backend.excecption.InvalidRequestDataException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public PresignedUrlResponse generatePresignedUploadUrl(String fileName,UUID entityID, UploadPurpose purpose) {
        String objectName = generateObjectName(fileName, purpose, entityID);

        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES) 
                            .build());

            return new PresignedUrlResponse(url, objectName);
        } catch (Exception e) {
            log.error("Error generating presigned URL for object {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Could not generate file upload URL", e);
        }
    }

    public String generatePresignedGetUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            log.error("Error generating presigned GET URL for object {}: {}", objectName, e.getMessage());
            return null;
        }
    }

    private String generateObjectName(String originalFileName, UploadPurpose purpose, UUID entityID) {
        String randomUuid = UUID.randomUUID().toString();
        String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        return switch (purpose) {
            case USER_AVATAR -> String.format("avatars/%s/%s-%s", entityID, randomUuid, sanitizedFileName);
            case COURSE_THUMBNAIL ->
                    String.format("courses/%s/thumbnail/%s-%s", entityID, randomUuid, sanitizedFileName);
            case LESSON_RESOURCE ->
                    String.format("lessons/%s/resources/%s-%s", entityID, randomUuid, sanitizedFileName);
        };
    }
}