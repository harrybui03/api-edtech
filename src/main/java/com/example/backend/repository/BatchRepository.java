package com.example.backend.repository;

import com.example.backend.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {}
