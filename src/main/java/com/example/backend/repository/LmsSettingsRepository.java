package com.example.backend.repository;

import com.example.backend.entity.LmsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LmsSettingsRepository extends JpaRepository<LmsSettings, UUID> {
} 