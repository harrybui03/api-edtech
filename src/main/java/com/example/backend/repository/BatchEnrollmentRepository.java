package com.example.backend.repository;

import com.example.backend.entity.Batch;
import com.example.backend.entity.BatchEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchEnrollmentRepository extends JpaRepository<BatchEnrollment, UUID> {
    boolean existsByUserIdAndBatchId(UUID userId, UUID batchId);

    @Query("SELECT e FROM BatchEnrollment e WHERE e.user.id = :memberId")
    Page<BatchEnrollment> findByMemberId(@Param("memberId") UUID memberId, Pageable pageable);

    @Query("SELECT e FROM BatchEnrollment e WHERE e.batch.id = :batchId")
    Page<BatchEnrollment> findByBatchId(@Param("batchId") UUID batchId, Pageable pageable);
    
    @Query("SELECT e.batch FROM BatchEnrollment e WHERE e.user.id = :userId")
    List<Batch> findBatchesByUserId(@Param("userId") UUID userId);
}
