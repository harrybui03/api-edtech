package com.example.backend.service;

import com.example.backend.dto.request.review.ReviewRequest;
import com.example.backend.dto.response.review.ReviewResponse;
import com.example.backend.entity.Course;
import com.example.backend.entity.Review;
import com.example.backend.entity.User;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.ReviewMapper;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.EnrollmentRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public ReviewResponse createReview(UUID courseId, ReviewRequest request) {
        User currentUser = getCurrentUser();
        Course course = findCourseById(courseId);
        
        // Check if user is enrolled in the course
        verifyEnrollment(courseId, currentUser.getId());
        
        // Check if user has already reviewed this course
        if (reviewRepository.existsByCourseIdAndStudentId(courseId, currentUser.getId())) {
            throw new InvalidRequestDataException("You have already reviewed this course");
        }

        Review review = ReviewMapper.toEntity(request);
        review.setCourse(course);
        review.setStudent(currentUser);
        review.setModifiedBy(currentUser.getId());

        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toResponse(savedReview);
    }

    @Transactional
    public ReviewResponse createReviewBySlug(String courseSlug, ReviewRequest request) {
        User currentUser = getCurrentUser();
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        verifyEnrollment(course.getId(), currentUser.getId());

        if (reviewRepository.existsByCourseIdAndStudentId(course.getId(), currentUser.getId())) {
            throw new InvalidRequestDataException("You have already reviewed this course");
        }

        Review review = ReviewMapper.toEntity(request);
        review.setCourse(course);
        review.setStudent(currentUser);
        review.setModifiedBy(currentUser.getId());

        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, ReviewRequest request) {
        User currentUser = getCurrentUser();
        Review review = findReviewById(reviewId);
        
        // Verify ownership
        if (!review.getStudent().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own reviews");
        }

        ReviewMapper.updateEntityFromRequest(request, review);
        review.setModifiedBy(currentUser.getId());

        Review updatedReview = reviewRepository.save(review);
        return ReviewMapper.toResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        User currentUser = getCurrentUser();
        Review review = findReviewById(reviewId);
        
        // Verify ownership
        if (!review.getStudent().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getApprovedReviewsByCourseSlug(String courseSlug, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByCourseSlugOrderByCreationDesc(courseSlug, pageable);
        return reviews.map(ReviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getMyReviewForCourse(UUID courseId) {
        User currentUser = getCurrentUser();
        Review review = reviewRepository.findByCourseIdAndStudentId(courseId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have not reviewed this course yet"));
        
        return ReviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getMyReviewForCourseSlug(String courseSlug) {
        User currentUser = getCurrentUser();
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Review review = reviewRepository.findByCourseIdAndStudentId(course.getId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have not reviewed this course yet"));
        return ReviewMapper.toResponse(review);
    }


    @Transactional(readOnly = true)
    public Double getAverageRatingForCourse(UUID courseId) {
        return reviewRepository.findAverageRatingByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForCourseSlug(String courseSlug) {
        Course course = courseRepository.findBySlug(courseSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with slug: " + courseSlug));
        return reviewRepository.findAverageRatingByCourseId(course.getId());
    }

    @Transactional(readOnly = true)
    public long getReviewCountForCourse(UUID courseId) {
        return reviewRepository.countByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllByOrderByCreationDesc(pageable);
        return reviews.map(ReviewMapper::toResponse);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Course findCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    private Review findReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
    }

    private void verifyEnrollment(UUID courseId, UUID userId) {
        boolean isEnrolled = enrollmentRepository.existsByMemberIdAndCourseId(userId, courseId);
        if (!isEnrolled) {
            throw new ForbiddenException("You must be enrolled in the course to leave a review");
        }
    }
}
