package com.example.backend.service;

import com.example.backend.config.RabbitMQConfig;
import com.example.backend.constant.UploadPurpose;
import com.example.backend.constant.job.JobStatus;
import com.example.backend.constant.job.JobType;
import com.example.backend.dto.message.TranscodingRequestMessage;
import com.example.backend.dto.request.upload.TranscodeRequest;
import com.example.backend.dto.response.upload.PresignedUrlResponse;
import com.example.backend.entity.Job;
import com.example.backend.entity.User;
import com.example.backend.excecption.InternalServerError;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.repository.JobRepository;
import com.example.backend.repository.UserRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    private final JobRepository jobRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MinioClient minioClient;
    private final UserRepository userRepository;

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




    @Transactional
    public void uploadAndQueueForTranscoding(TranscodeRequest transcodeRequest) {
        MultipartFile file = transcodeRequest.getFile();
        String objectPath = generateObjectName(file.getName(),  transcodeRequest.getPurpose(), transcodeRequest.getEntityId());
        Job job = Job.builder()
                .entityId(transcodeRequest.getEntityId())
                .entityType(transcodeRequest.getPurpose())
                .status(JobStatus.PENDING)
                .user(getCurrentUser())
                .jobType(JobType.VIDEO_TRANSCODING)
                .build();

        jobRepository.save(job);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            job.setStatus(JobStatus.FAILED);
            jobRepository.save(job);
            throw new InternalServerError("Could not upload video file.", e.getMessage());
        }

        TranscodingRequestMessage message = new TranscodingRequestMessage(job.getId(), objectPath);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }

    private String generateObjectName(String originalFileName, UploadPurpose purpose, UUID entityID) {
        String currentTimestamp = String.valueOf(System.currentTimeMillis());
        String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");

        return switch (purpose) {
            case USER_AVATAR -> String.format("avatars/%s/%s-%s", entityID, currentTimestamp, sanitizedFileName);
            case COURSE_THUMBNAIL ->
                    String.format("courses/%s/thumbnail/%s-%s", entityID, currentTimestamp, sanitizedFileName);
            case LESSON_RESOURCE ->
                    String.format("lessons/%s/resources/%s-%s", entityID, currentTimestamp, sanitizedFileName);
            case LESSON_VIDEO ->
                    String.format("lessons/%s/videos/%s-%s" , entityID , currentTimestamp , sanitizedFileName);
            case BATCH_DISCUSSION ->
                    String.format("batch-discussions/%s/%s", entityID , currentTimestamp);
        };
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }
}