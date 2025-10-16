package com.example.backend.repository;

import com.example.backend.entity.BatchEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BatchEnrollmentRepository extends JpaRepository<BatchEnrollment, UUID> {
    boolean existsByUserIdAndBatchId(UUID userId, UUID batchId);
}
