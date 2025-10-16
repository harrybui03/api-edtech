package com.example.backend.repository;

import com.example.backend.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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

}
