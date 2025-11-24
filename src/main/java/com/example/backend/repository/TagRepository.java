package com.example.backend.repository;

import com.example.backend.constant.EntityType;
import com.example.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByEntityIdAndEntityType(UUID entityId, EntityType entityType);

    void deleteByEntityIdAndEntityType(UUID entityId, EntityType entityType);

    @Modifying
    void deleteByEntityIdAndEntityTypeAndNameIn(UUID entityId , EntityType entityType , List<String> names);
}