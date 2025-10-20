package com.example.backend.repository;

import com.example.backend.entity.BatchDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchDocumentRepository extends JpaRepository<BatchDocument, UUID> {
    List<BatchDocument> findByBatchDiscussion_Id(UUID batchDiscussionId);
}
