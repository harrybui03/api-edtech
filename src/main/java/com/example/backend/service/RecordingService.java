package com.example.backend.service;

import com.example.backend.config.RabbitMQConfig;
import com.example.backend.constant.UploadPurpose;
import com.example.backend.constant.job.JobStatus;
import com.example.backend.constant.job.JobType;
import com.example.backend.dto.message.RecordingProcessingMessage;
import com.example.backend.entity.Job;
import com.example.backend.entity.LiveSession;
import com.example.backend.repository.JobRepository;
import com.example.backend.repository.LiveSessionRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordingService {
    
private final MinioClient minioClient;
private final LiveSessionRepository liveSessionRepository;
private final JobRepository jobRepository;
private final RabbitTemplate rabbitTemplate;
    
@Value("${minio.bucket.name}")
private String bucketName;
    
@Value("${janus.recording.dir:janus-recordings}")
private String janusRecordingDir;
    
/**
* Process recording asynchronously after live session ends
* 1. Find .mjr files from Janus
* 2. Upload RAW .mjr to MinIO
* 3. Create Job for tracking
* 4. Send message to RabbitMQ for worker processing
* 5. Clean up local files
* 
* Worker will: download .mjr, convert to .mp4, upload .mp4, update database
*/
@Async
public void processRecording(UUID liveSessionId) {
LiveSession session = liveSessionRepository.findById(liveSessionId)
.orElseThrow(() -> new RuntimeException("Live session not found: " + liveSessionId));
        
try {
session.setRecordingStatus(LiveSession.RecordingStatus.PROCESSING);
liveSessionRepository.save(session);
            
Long roomId = session.getRoomId();
            
// 1. Find .mjr files for this room
Path recordingDir = Paths.get(janusRecordingDir);
File recordingDirFile = recordingDir.toFile();
            
if (!recordingDirFile.exists() || !recordingDirFile.isDirectory()) {
session.setRecordingStatus(LiveSession.RecordingStatus.FAILED);
liveSessionRepository.save(session);
return;
}
            
File[] mjrFiles = recordingDirFile.listFiles((dir, name) -> 
name.contains(roomId.toString()) && name.endsWith(".mjr")
);
            
if (mjrFiles == null || mjrFiles.length == 0) {
session.setRecordingStatus(LiveSession.RecordingStatus.FAILED);
liveSessionRepository.save(session);
return;
}
            
// Find the video file (usually contains "-video" in filename)
File videoMjrFile = findVideoFile(mjrFiles);
            
if (videoMjrFile == null) {
session.setRecordingStatus(LiveSession.RecordingStatus.FAILED);
liveSessionRepository.save(session);
return;
}
            
// 2. Upload RAW .mjr file to MinIO
String mjrObjectName = "live-recordings-raw/" + roomId + "/" + videoMjrFile.getName();
            
try (InputStream inputStream = new FileInputStream(videoMjrFile)) {
minioClient.putObject(
PutObjectArgs.builder()
.bucket(bucketName)
.object(mjrObjectName)
.stream(inputStream, videoMjrFile.length(), -1)
.contentType("application/octet-stream")
.build()
);
}
            
// 3. Create Job for tracking
Job job = Job.builder()
.entityId(session.getId())
.entityType(UploadPurpose.LESSON_VIDEO) // Reuse existing enum or add new one
.status(JobStatus.PENDING)
.jobType(JobType.RECORDING_PROCESSING)
.user(session.getInstructor())
.build();
jobRepository.save(job);
            
// 4. Send message to RabbitMQ for worker processing
RecordingProcessingMessage message = new RecordingProcessingMessage(
job.getId(),
session.getId(),
mjrObjectName,
roomId
);
            
rabbitTemplate.convertAndSend(
RabbitMQConfig.EXCHANGE_NAME,
RabbitMQConfig.RECORDING_ROUTING_KEY,
message
);
            
// 5. Clean up local .mjr files
for (File file : mjrFiles) {
file.delete();
}
            
} catch (Exception e) {
session.setRecordingStatus(LiveSession.RecordingStatus.FAILED);
liveSessionRepository.save(session);
}
}
    
/**
* Find the video .mjr file (usually contains "-video" in filename)
*/
private File findVideoFile(File[] mjrFiles) {
for (File file : mjrFiles) {
if (file.getName().contains("-video")) {
return file;
}
}
// If no explicit video file, return the first one
return mjrFiles.length > 0 ? mjrFiles[0] : null;
}
}

