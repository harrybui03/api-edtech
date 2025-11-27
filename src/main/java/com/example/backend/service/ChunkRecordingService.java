package com.example.backend.service;

import com.example.backend.config.RabbitMQConfig;
import com.example.backend.constant.UploadPurpose;
import com.example.backend.constant.job.JobStatus;
import com.example.backend.constant.job.JobType;
import com.example.backend.dto.message.RecordingMergeMessage;
import com.example.backend.dto.response.live.ChunkUploadResponse;
import com.example.backend.dto.response.live.RecordingStatusResponse;
import com.example.backend.entity.Job;
import com.example.backend.entity.LiveSession;
import com.example.backend.entity.RecordingChunk;
import com.example.backend.entity.User;
import com.example.backend.excecption.DataNotFoundException;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.repository.JobRepository;
import com.example.backend.repository.LiveSessionRepository;
import com.example.backend.repository.RecordingChunkRepository;
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

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkRecordingService {
    
    private final MinioClient minioClient;
    private final LiveSessionRepository liveSessionRepository;
    private final RecordingChunkRepository recordingChunkRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${minio.bucket.name}")
    private String bucketName;
    
    /**
     * Upload a recording chunk from frontend
     */
    @Transactional
    public ChunkUploadResponse uploadChunk(Long roomId, Integer chunkIndex, Integer durationSeconds, MultipartFile file) {
        User currentUser = getCurrentUser();
        
        // Get live session and validate instructor
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        if (!liveSession.getInstructor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the instructor can upload recording chunks");
        }
        
        // Set status to RECORDING if not already set
        if (liveSession.getRecordingStatus() == null || 
            liveSession.getRecordingStatus() == LiveSession.RecordingStatus.NOT_STARTED) {
            liveSession.setRecordingStatus(LiveSession.RecordingStatus.RECORDING);
            liveSessionRepository.save(liveSession);
        }
        
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Chunk file is empty");
            }
            
            long fileSize = file.getSize();
            if (fileSize < 1000) { // Less than 1KB is suspicious
                throw new RuntimeException(
                    String.format("Chunk file is too small (%d bytes), may be corrupt", fileSize)
                );
            }
            
            // Validate content type
            String contentType = file.getContentType();
            if (contentType != null && !contentType.contains("webm") && !contentType.contains("octet-stream")) {
                log.warn("Unexpected content type for chunk {}: {}", chunkIndex, contentType);
            }
            
            log.info("Uploading chunk {} for live session {}: {} bytes", 
                    chunkIndex, liveSession.getId(), fileSize);
            
            // Upload to MinIO
            String objectName = String.format(
                "live-recordings/%s/chunks/chunk_%04d.webm", 
                liveSession.getId(), 
                chunkIndex
            );
            
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, fileSize, -1)
                        .contentType("video/webm")
                        .build()
                );
            }
            
            // Save chunk metadata
            RecordingChunk chunk = RecordingChunk.builder()
                    .liveSession(liveSession)
                    .chunkIndex(chunkIndex)
                    .objectName(objectName)
                    .fileSize(file.getSize())
                    .durationSeconds(durationSeconds)
                    .status(RecordingChunk.ChunkStatus.UPLOADED)
                    .build();
            
            chunk = recordingChunkRepository.save(chunk);
            
            // Update total chunks count
            Integer totalChunks = recordingChunkRepository.countByLiveSessionId(liveSession.getId());
            liveSession.setTotalChunks(totalChunks);
            liveSessionRepository.save(liveSession);
            
            return ChunkUploadResponse.builder()
                    .chunkId(chunk.getId())
                    .chunkIndex(chunkIndex)
                    .message("Chunk uploaded successfully")
                    .fileSize(file.getSize())
                    .totalChunksUploaded(totalChunks)
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload chunk: " + e.getMessage(), e);
        }
    }
    
    /**
     * Complete recording and trigger merge process
     */
    @Transactional
    public RecordingStatusResponse completeRecording(Long roomId, Integer totalChunks, Integer totalDurationSeconds) {
        User currentUser = getCurrentUser();
        
        // Get live session and validate instructor
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        if (!liveSession.getInstructor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the instructor can complete recording");
        }
        
        // Validate all chunks are uploaded
        Integer uploadedChunks = recordingChunkRepository.countByLiveSessionId(liveSession.getId());
        if (!uploadedChunks.equals(totalChunks)) {
            throw new RuntimeException(
                String.format("Chunk count mismatch. Expected: %d, Uploaded: %d", totalChunks, uploadedChunks)
            );
        }

        // Update live session
        liveSession.setRecordingStatus(LiveSession.RecordingStatus.PROCESSING);
        liveSession.setTotalChunks(totalChunks);
        liveSession.setRecordingDuration(totalDurationSeconds);
        liveSessionRepository.save(liveSession);
        
        // Create job for tracking
        Job job = Job.builder()
                .entityId(liveSession.getId())
                .entityType(UploadPurpose.LESSON_VIDEO)
                .status(JobStatus.PENDING)
                .jobType(JobType.RECORDING_MERGE)
                .user(currentUser)
                .build();
        jobRepository.save(job);
        
        // Send message to RabbitMQ for worker to merge and transcode
        RecordingMergeMessage message = new RecordingMergeMessage(
                job.getId(),
                liveSession.getId()
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.RECORDING_MERGE_ROUTING_KEY,
                message
        );
        
        return RecordingStatusResponse.builder()
                .roomId(roomId)
                .status(LiveSession.RecordingStatus.PROCESSING)
                .message("Recording completed. Processing merge and transcoding...")
                .totalChunks(totalChunks)
                .durationSeconds(totalDurationSeconds)
                .build();
    }
    
    /**
     * Get recording status and URL if completed
     */
    public RecordingStatusResponse getRecordingStatus(Long roomId) {
        LiveSession liveSession = liveSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new DataNotFoundException("Live session not found with room ID: " + roomId));
        
        LiveSession.RecordingStatus status = liveSession.getRecordingStatus();
        if (status == null) {
            status = LiveSession.RecordingStatus.NOT_STARTED;
        }
        
        RecordingStatusResponse.RecordingStatusResponseBuilder responseBuilder = RecordingStatusResponse.builder()
                .roomId(roomId)
                .status(status)
                .totalChunks(liveSession.getTotalChunks())
                .durationSeconds(liveSession.getRecordingDuration());
        
        switch (status) {
            case NOT_STARTED:
                return responseBuilder
                        .message("Recording has not started yet")
                        .build();
                        
            case RECORDING:
                return responseBuilder
                        .message("Recording in progress...")
                        .build();
                        
            case PROCESSING:
                return responseBuilder
                        .message("Recording is being merged and transcoded. Please check back later.")
                        .build();
                        
            case FAILED:
                return responseBuilder
                        .message("Recording processing failed. Please contact support.")
                        .build();
                        
            case COMPLETED:
                String objectName = liveSession.getFinalVideoObjectName();
                
                if (objectName == null || objectName.isEmpty()) {
                    return responseBuilder
                            .status(LiveSession.RecordingStatus.FAILED)
                            .message("Final video path not found")
                            .build();
                }
                
                try {
                    String presignedUrl = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
                    );
                    
                    return responseBuilder
                            .videoUrl(presignedUrl)
                            .message("Recording is ready")
                            .build();
                            
                } catch (Exception e) {
                    return responseBuilder
                            .status(LiveSession.RecordingStatus.FAILED)
                            .message("Error generating video URL: " + e.getMessage())
                            .build();
                }
                
            default:
                return responseBuilder
                        .message("Unknown recording status")
                        .build();
        }
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found with email: " + email));
    }
}

