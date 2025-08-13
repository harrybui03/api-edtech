package com.example.backend.repository;

import com.example.backend.entity.LiveClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LiveClassRepository extends JpaRepository<LiveClass, UUID> {
} 