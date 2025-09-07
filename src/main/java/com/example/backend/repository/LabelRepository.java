package com.example.backend.repository;

import com.example.backend.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
}