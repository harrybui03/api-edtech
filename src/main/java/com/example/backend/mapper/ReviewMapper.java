package com.example.backend.mapper;

import com.example.backend.dto.model.ReviewDto;
import com.example.backend.dto.request.review.ReviewRequest;
import com.example.backend.dto.response.review.ReviewResponse;
import com.example.backend.entity.Review;

public final class ReviewMapper {

    public static ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setCourseId(review.getCourse() != null ? review.getCourse().getId() : null);
        dto.setStudentId(review.getStudent() != null ? review.getStudent().getId() : null);
        dto.setStudentName(review.getStudent() != null ? review.getStudent().getFullName() : null);
        dto.setStudentImage(review.getStudent() != null ? review.getStudent().getUserImage() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setIsApproved(review.getIsApproved());
        dto.setApprovedBy(review.getApprovedBy() != null ? review.getApprovedBy().getId() : null);
        dto.setApprovedAt(review.getApprovedAt());
        dto.setCreation(review.getCreation());
        dto.setModified(review.getModified());
        return dto;
    }

    public static ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setCourseId(review.getCourse() != null ? review.getCourse().getId() : null);
        response.setCourseName(review.getCourse() != null ? review.getCourse().getTitle() : null);
        response.setStudentId(review.getStudent() != null ? review.getStudent().getId() : null);
        response.setStudentName(review.getStudent() != null ? review.getStudent().getFullName() : null);
        response.setStudentImage(review.getStudent() != null ? review.getStudent().getUserImage() : null);
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setIsApproved(review.getIsApproved());
        response.setCreation(review.getCreation());
        response.setModified(review.getModified());
        return response;
    }

    public static Review toEntity(ReviewRequest request) {
        if (request == null) {
            return null;
        }
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsApproved(true); // Auto-approve all reviews
        return review;
    }

    public static void updateEntityFromRequest(ReviewRequest request, Review review) {
        if (request == null || review == null) {
            return;
        }
        review.setRating(request.getRating());
        review.setComment(request.getComment());
    }
}
