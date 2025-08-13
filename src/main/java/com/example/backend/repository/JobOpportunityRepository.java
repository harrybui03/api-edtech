package com.example.backend.repository;

import com.example.backend.entity.JobOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobOpportunityRepository extends JpaRepository<JobOpportunity, UUID> {
} 