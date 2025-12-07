package com.example.backend.repository;

import com.example.backend.dto.response.statistics.PerformanceReportItem;
import com.example.backend.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.backend.constant.BatchStatus;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID>, JpaSpecificationExecutor<Batch> {

    Optional<Batch> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT b FROM Batch b JOIN b.instructors bi WHERE bi.instructor.id = :instructorId AND (:status IS NULL OR b.status = :status)")
    Page<Batch> findBatchesByInstructorAndStatus(UUID instructorId, BatchStatus status, Pageable pageable);

    long countByInstructors_Instructor_IdAndStatus(UUID instructorId, BatchStatus status);

    @Query(value = "SELECT new com.example.backend.dto.response.statistics.PerformanceReportItem(" +
            "b.id, b.title, " +
            "COALESCE(SUM(t.amount), 0), " +
            "(SELECT COUNT(t2.id) FROM Transaction t2 WHERE t2.batch.id = b.id AND t2.status = 'PAID')) " +
            "FROM Batch b " +
            "JOIN b.instructors bi " +
            "LEFT JOIN Transaction t ON t.batch = b AND t.status = 'PAID' " +
            "WHERE bi.instructor.id = :instructorId " +
            "GROUP BY b.id, b.title, b.createdAt",
            countQuery = "SELECT COUNT(b) FROM Batch b JOIN b.instructors bi WHERE bi.instructor.id = :instructorId")
    Page<PerformanceReportItem> getBatchPerformanceReport(@Param("instructorId") UUID instructorId, Pageable pageable);
}
