package com.example.backend.repository;

import com.example.backend.constant.CourseStatus;
import com.example.backend.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
//    @Query("SELECT c FROM Course c WHERE c.published = true " +
//            "AND (:#{#category} IS NULL OR c.category = :#{#category}) " +
//            "AND (:#{#search} IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :#{#search}, '%')))")
//    Page<Course> findPublishedCourses(@Param("category") String category, @Param("search") String search, Pageable pageable);
//
//    @Query("SELECT c FROM Course c JOIN c.instructors i WHERE i.user.id = :instructorId AND (:status IS NULL OR c.status = :status)")
//    Page<Course> findCoursesByInstructorAndStatus(@Param("instructorId") UUID instructorId, @Param("status") CourseStatus status, Pageable pageable);
}
