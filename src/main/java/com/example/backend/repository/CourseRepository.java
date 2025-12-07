package com.example.backend.repository;

import com.example.backend.constant.CourseStatus;
import com.example.backend.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import com.example.backend.dto.response.statistics.PerformanceReportItem;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    @Query("SELECT c FROM Course c JOIN c.instructors i WHERE i.user.id = :instructorId AND (:status IS NULL OR c.status = :status)")
    Page<Course> findCoursesByInstructorAndStatus(@Param("instructorId") UUID instructorId, @Param("status") CourseStatus status, Pageable pageable);

    Optional<Course> findBySlug(String slug);

    @Query("SELECT c FROM Course c WHERE c.slug = :slug AND c.status = 'PUBLISHED'")
    Optional<Course> findBySlugAndStatusPublished(@Param("slug") String slug);

    boolean existsBySlug(String slug);

    long countByInstructors_User_IdAndStatus(UUID instructorId, CourseStatus status);

    @Query(value = "SELECT new com.example.backend.dto.response.statistics.PerformanceReportItem(" +
            "c.id, c.title, " +
            "COALESCE(SUM(t.amount), 0), " +
            "(SELECT COUNT(e.id) FROM Enrollment e WHERE e.course.id = c.id)) " +
            "FROM Course c " +
            "JOIN c.instructors ci " +
            "LEFT JOIN Transaction t ON t.course = c AND t.status = 'PAID' " +
            "WHERE ci.user.id = :instructorId " +
            "GROUP BY c.id, c.title, c.creation",
            countQuery = "SELECT COUNT(c) FROM Course c JOIN c.instructors ci WHERE ci.user.id = :instructorId")
    Page<PerformanceReportItem> getCoursePerformanceReport(@Param("instructorId") UUID instructorId, Pageable pageable);
}
