package com.example.backend.controller;

import com.example.backend.constant.BatchStatus;
import com.example.backend.constant.CourseStatus;
import com.example.backend.dto.model.*;
import com.example.backend.dto.request.batch.CreateBatchRequest;
import com.example.backend.dto.request.batch.UpdateBatchRequest;
import com.example.backend.dto.request.course.ChapterRequest;
import com.example.backend.dto.request.course.CourseRequest; 
import com.example.backend.dto.request.course.LessonRequest;
import com.example.backend.dto.request.instructor.InstructorIdsRequest;
import com.example.backend.dto.request.quiz.QuizRequest;
import com.example.backend.dto.request.quiz.QuizQuestionRequest;
import com.example.backend.dto.request.quiz.QuizQuestionsRequest;
import com.example.backend.dto.request.payos.CreatePayOSConfigRequest;
import com.example.backend.dto.response.enrollment.EnrollmentResponse;
import com.example.backend.dto.response.enrollment.BatchEnrollmentResponse;
import com.example.backend.dto.response.pagination.PaginationResponse;
import com.example.backend.dto.response.statistics.PerformanceReportItem;
import com.example.backend.dto.response.payos.PayOSConfigResponse;
import com.example.backend.dto.request.payos.UpdatePayOSConfigRequest;
import com.example.backend.dto.response.statistics.RevenueOverTimeResponse;
import com.example.backend.dto.response.quiz.QuizSubmissionResponse;
import com.example.backend.dto.response.statistics.InstructorStatsResponse;
import com.example.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instructor")
@RequiredArgsConstructor
public class InstructorController {

    private final CourseService courseService;
    private final ChapterService chapterService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final QuizService quizService;
    private final BatchService batchService;
    private final PayOSConfigService payOSConfigService;
    private final JobService jobService;
    private final StatisticsService statisticsService;

    @PostMapping("/courses")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseRequest request) {
        return new ResponseEntity<>(courseService.createCourse(request), HttpStatus.CREATED);
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable UUID courseId, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<PaginationResponse<CourseDto>> getMyCourses(Pageable pageable, @RequestParam(required = false) CourseStatus status) {
        Page<CourseDto> courses = courseService.getMyCourses(pageable, status);
        return ResponseEntity.ok(new PaginationResponse<>(courses));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get course by ID for instructor", description = "Instructor gets their course details, including all chapters and lessons.")
    public ResponseEntity<CourseDto> getCourseByIdForInstructor(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourseByIdForInstructor(courseId));
    }

    @GetMapping("/chapters/{chapterId}")
    @Operation(summary = "Get chapter by ID for instructor", description = "Instructor gets their chapter details, including all lessons.")
    public ResponseEntity<ChapterDto> getChapterByIdForInstructor(@PathVariable UUID chapterId) {
        return ResponseEntity.ok(chapterService.getChapterByIdForInstructor(chapterId));
    }

    @PutMapping("/courses/{courseId}/publish")
    public ResponseEntity<Void> publishCourse(@PathVariable UUID courseId) {
        courseService.publishCourse(courseId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/courses/{courseId}/instructors")
    @Operation(summary = "Upsert instructors for a course", description = "Sets the list of instructors for a course. This will add new instructors and remove those not in the list.")
    public ResponseEntity<Void> upsertInstructorsForCourse(@PathVariable UUID courseId, @RequestBody InstructorIdsRequest request) {
        courseService.upsertInstructors(courseId, request.getInstructorIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    @Operation(summary = "Get all instructors", description = "Retrieves a list of all users who have the instructor role (COURSE_CREATOR).")
    public ResponseEntity<List<UserDTO>> getAllInstructors() {
        List<UserDTO> instructors = courseService.getAllInstructors();
        return ResponseEntity.ok(instructors);
    }

    @PostMapping("/courses/{courseId}/chapters")
    public ResponseEntity<ChapterDto> createChapter(@PathVariable UUID courseId, @RequestBody ChapterRequest request) {
        return new ResponseEntity<>(chapterService.createChapter(courseId, request), HttpStatus.CREATED);
    }

    @PutMapping("/chapters/{chapterId}")
    public ResponseEntity<ChapterDto> updateChapter(@PathVariable UUID chapterId, @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, request));
    }

    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Void> deleteChapter(@PathVariable UUID chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chapters/{chapterId}/lessons")
    public ResponseEntity<LessonDto> createLesson(@PathVariable UUID chapterId, @RequestBody LessonRequest request) {
        return new ResponseEntity<>(lessonService.createLesson(chapterId, request), HttpStatus.CREATED);
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable UUID lessonId, @RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request));
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Get lesson by ID for instructor", description = "Instructor gets their lesson details.")
    public ResponseEntity<LessonDto> getLessonByIdForInstructor(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonByIdForInstructor(lessonId));
    }
    
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/enrollments")
    @Operation(summary = "Get course enrollments", description = "Instructor gets all enrollments for their course")
    public ResponseEntity<List<EnrollmentResponse>> getCourseEnrollments(@PathVariable UUID courseId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId);
        return ResponseEntity.ok(enrollments);
    }
    
    @DeleteMapping("/enrollments/{enrollmentId}")
    @Operation(summary = "Remove enrollment", description = "Instructor removes a student from their course")
    public ResponseEntity<Void> removeEnrollment(@PathVariable UUID enrollmentId) {
        enrollmentService.removeEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/batches/{batchId}/enrollments")
    @Operation(summary = "Get batch enrollments", description = "Instructor gets all enrollments for their batch")
    public ResponseEntity<Page<BatchEnrollmentResponse>> getBatchEnrollments(@PathVariable UUID batchId, Pageable pageable) {
        Page<BatchEnrollmentResponse> enrollments = enrollmentService.getBatchEnrollments(batchId, pageable);
        return ResponseEntity.ok(enrollments);
    }

    // Quiz Management APIs
    @PostMapping("/quizzes")
    @Operation(summary = "Create a new quiz", description = "Instructor creates a new quiz for a course")
    public ResponseEntity<QuizDto> createQuiz(@RequestBody QuizRequest request) {
        QuizDto quiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    @PutMapping("/quizzes/{quizId}")
    @Operation(summary = "Update quiz", description = "Instructor updates an existing quiz")
    public ResponseEntity<QuizDto> updateQuiz(@PathVariable UUID quizId, @RequestBody QuizRequest request) {
        QuizDto quiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/quizzes/{quizId}")
    @Operation(summary = "Delete quiz", description = "Instructor deletes a quiz")
    public ResponseEntity<Void> deleteQuiz(@PathVariable UUID quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/quizzes/{quizId}/questions")
    @Operation(summary = "Get all questions for a quiz", description = "Instructor retrieves all questions for a specific quiz.")
    public ResponseEntity<List<QuizQuestionDto>> getQuestionsByQuizId(@PathVariable UUID quizId) {
        List<QuizQuestionDto> questions = quizService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @Operation(summary = "Add questions to quiz", description = "Instructor adds multiple questions to a quiz")
    public ResponseEntity<QuizDto> addQuestionsToQuiz(@PathVariable UUID quizId, @RequestBody QuizQuestionsRequest request) {
        QuizDto quiz = quizService.addQuestionsToQuiz(quizId, request.getQuestions());
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    @PutMapping("/questions/{questionId}")
    @Operation(summary = "Update question", description = "Instructor updates a quiz question")
    public ResponseEntity<Void> updateQuestion(@PathVariable UUID questionId, @RequestBody QuizQuestionRequest request) {
        quizService.updateQuestion(questionId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "Delete question", description = "Instructor deletes a quiz question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/quiz-submissions")
    @Operation(summary = "Get quiz submissions for course", description = "Instructor gets all quiz submissions for their course")
    public ResponseEntity<List<QuizSubmissionResponse>> getCourseQuizSubmissions(@PathVariable UUID courseId) {
        List<QuizSubmissionResponse> submissions = quizService.getCourseQuizSubmissions(courseId);
        return ResponseEntity.ok(submissions);
    }

    // Batch Management APIs
    @PostMapping("/batches")
    @Operation(summary = "Create a new batch", description = "Instructor creates a new batch")
    public ResponseEntity<BatchDto> createBatch(@RequestBody CreateBatchRequest request) {
        return new ResponseEntity<>(batchService.createBatch(request), HttpStatus.CREATED);
    }

    @PutMapping("/batches/{batchId}")
    @Operation(summary = "Update batch", description = "Instructor updates an existing batch")
    public ResponseEntity<BatchDto> updateBatch(@PathVariable UUID batchId, @RequestBody UpdateBatchRequest request) {
        return ResponseEntity.ok(batchService.updateBatch(batchId, request));
    }

    @DeleteMapping("/batches/{batchId}")
    @Operation(summary = "Delete batch", description = "Instructor deletes a batch")
    public ResponseEntity<Void> deleteBatch(@PathVariable UUID batchId) {
        batchService.deleteBatch(batchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-batches")
    @Operation(summary = "Get my batches", description = "Instructor gets a paginated list of their batches, optionally filtered by status")
    public ResponseEntity<PaginationResponse<BatchDto>> getMyBatches(Pageable pageable, @RequestParam(required = false) BatchStatus status) {
        Page<BatchDto> batches = batchService.getMyBatches(pageable, status);
        return ResponseEntity.ok(new PaginationResponse<>(batches));
    }

    @GetMapping("/batches/{batchId}")
    @Operation(summary = "Get batch by ID for instructor", description = "Instructor gets their batch details.")
    public ResponseEntity<BatchDto> getBatchById(@PathVariable UUID batchId) {
        return ResponseEntity.ok(batchService.getBatchById(batchId));
    }

    @PutMapping("/batches/{batchId}/publish")
    @Operation(summary = "Publish a batch", description = "Instructor publishes a batch, making it visible to the public.")
    public ResponseEntity<Void> publishBatch(@PathVariable UUID batchId) {
        batchService.publishBatch(batchId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batches/{batchId}/instructors")
    @Operation(summary = "Add instructors to batch", description = "Instructor adds one or more instructors to a batch")
    public ResponseEntity<Void> addInstructorsToBatch(@PathVariable UUID batchId, @RequestBody InstructorIdsRequest request) {
        batchService.addInstructorsToBatch(batchId, request.getInstructorIds());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/batches/{batchId}/instructors/{instructorId}")
    @Operation(summary = "Remove instructor from batch", description = "Instructor removes another instructor from a batch")
    public ResponseEntity<Void> removeInstructorFromBatch(@PathVariable UUID batchId, @PathVariable UUID instructorId) {
        batchService.removeInstructorFromBatch(batchId, instructorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics/overview")
    @Operation(summary = "Get instructor statistics overview", description = "Retrieves an overview of the instructor's published content and revenue.")
    public ResponseEntity<InstructorStatsResponse> getInstructorStatsOverview() {
        return ResponseEntity.ok(statisticsService.getInstructorOverviewStats());
    }

    @GetMapping("/statistics/revenue-over-time")
    @Operation(summary = "Get instructor revenue over a time period", description = "Retrieves revenue data points for charting, filterable by period and type.")
    public ResponseEntity<RevenueOverTimeResponse> getRevenueOverTime(
            @RequestParam(defaultValue = "MONTH") String period, // WEEK, MONTH, YEAR, ALL_TIME
            @RequestParam(defaultValue = "COURSE") String type // COURSE, BATCH
    ) {
        RevenueOverTimeResponse response = statisticsService.getRevenueOverTime(period, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/course-performance")
    @Operation(summary = "Get course performance report", description = "Retrieves a paginated and sortable list of course performance metrics.")
    public ResponseEntity<PaginationResponse<PerformanceReportItem>> getCoursePerformance(Pageable pageable) {
        Page<PerformanceReportItem> report = statisticsService.getCoursePerformanceReport(pageable);
        return ResponseEntity.ok(new PaginationResponse<>(report));
    }

    @GetMapping("/statistics/batch-performance")
    @Operation(summary = "Get batch performance report", description = "Retrieves a paginated and sortable list of batch performance metrics.")
    public ResponseEntity<PaginationResponse<PerformanceReportItem>> getBatchPerformance(Pageable pageable) {
        Page<PerformanceReportItem> report = statisticsService.getBatchPerformanceReport(pageable);
        return ResponseEntity.ok(new PaginationResponse<>(report));
    }

    // PayOS Configuration APIs
    @PostMapping("/payos-configs")
    @Operation(summary = "Create PayOS configuration", description = "Create a new PayOS configuration for an instructor")
        public ResponseEntity<PayOSConfigResponse> createPayOSConfig(@RequestBody CreatePayOSConfigRequest request) {
        PayOSConfigResponse response = payOSConfigService.createPayOSConfig(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payos-configs/my-config")
    @Operation(summary = "Get my PayOS configuration", description = "Get current user's active PayOS configuration")
    public ResponseEntity<PayOSConfigResponse> getMyPayOSConfig() {
        PayOSConfigResponse response = payOSConfigService.getMyPayOSConfig();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-jobs")
    @Operation(summary = "Get my jobs", description = "Instructor gets a paginated list of their created jobs (e.g., video transcoding).")
    public ResponseEntity<PaginationResponse<JobDto>> getMyJobs(Pageable pageable) {
        Page<JobDto> jobs = jobService.getMyJobs(pageable);
        return ResponseEntity.ok(new PaginationResponse<>(jobs));
    }

    @PutMapping("/payos-configs/{configId}")
    @Operation(summary = "Update PayOS configuration", description = "Update an existing PayOS configuration for the current instructor")
    public ResponseEntity<PayOSConfigResponse> updatePayOSConfig(@PathVariable UUID configId, @RequestBody UpdatePayOSConfigRequest request) {
        PayOSConfigResponse response = payOSConfigService.updatePayOSConfig(configId, request);
        return ResponseEntity.ok(response);
    }
}
