package com.example.backend.repository;

import com.example.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
}