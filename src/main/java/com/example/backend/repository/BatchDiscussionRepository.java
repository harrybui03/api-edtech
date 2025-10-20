package com.example.backend.repository;

import com.example.backend.entity.BatchDiscussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BatchDiscussionRepository extends JpaRepository<BatchDiscussion, UUID> {
    Page<BatchDiscussion> findByBatchIdAndReplyToIsNullOrderByCreatedAtDesc(UUID batchId, Pageable pageable);

    Page<BatchDiscussion> findByReplyToIdOrderByCreatedAtAsc(UUID discussionId, Pageable pageable);
}