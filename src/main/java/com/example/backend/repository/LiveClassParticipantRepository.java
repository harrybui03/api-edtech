package com.example.backend.repository;

import com.example.backend.entity.LiveClassParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LiveClassParticipantRepository extends JpaRepository<LiveClassParticipant, UUID> {
} 