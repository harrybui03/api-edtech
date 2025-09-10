package com.example.backend.service;

import com.example.backend.dto.request.progress.VideoWatchTimeRequest;
import com.example.backend.dto.response.progress.CourseProgressResponse;

import java.util.UUID;

public interface ProgressTrackingService {
    
    /**
     * Mark a lesson as completed for a student
     */
    void markLessonCompleted(UUID studentId, UUID lessonId);
    
    /**
     * Get detailed progress for a student in a specific course
     */
    CourseProgressResponse getCourseProgress(UUID studentId, UUID courseId);
    
    /**
     * Record video watch time for a lesson
     */
    void recordVideoWatchTime(UUID studentId, UUID lessonId, VideoWatchTimeRequest request);
}
