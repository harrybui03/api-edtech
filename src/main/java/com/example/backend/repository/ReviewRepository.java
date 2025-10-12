package com.example.backend.repository;

import com.example.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Find all reviews for a course (for public display)
    Page<Review> findByCourseIdOrderByCreationDesc(UUID courseId, Pageable pageable);
    
    // Find all reviews for a course by slug (for public display)
    @Query("SELECT r FROM Review r JOIN r.course c WHERE c.slug = :courseSlug ORDER BY r.creation DESC")
    Page<Review> findByCourseSlugOrderByCreationDesc(@Param("courseSlug") String courseSlug, Pageable pageable);

    // Find student's own review for a course
    Optional<Review> findByCourseIdAndStudentId(UUID courseId, UUID studentId);

    // Find all reviews for admin
    Page<Review> findAllByOrderByCreationDesc(Pageable pageable);

    // Find all reviews by a specific student
    Page<Review> findByStudentIdOrderByCreationDesc(UUID studentId, Pageable pageable);

    // Check if a student has already reviewed a course
    boolean existsByCourseIdAndStudentId(UUID courseId, UUID studentId);

    // Count reviews for a course
    long countByCourseId(UUID courseId);

    // Calculate average rating for a course
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") UUID courseId);
}
