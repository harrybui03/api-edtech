package com.example.backend.repository;

import com.example.backend.entity.VideoWatchDuration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoWatchDurationRepository extends JpaRepository<VideoWatchDuration, UUID> {
} 