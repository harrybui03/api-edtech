package com.example.backend.service;

import com.example.backend.constant.EntityType;
import com.example.backend.entity.Label;
import com.example.backend.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class LabelService {
    private final LabelRepository labelRepository;

    public List<Label> upsertLabels(List<String> labelNames, UUID entityId , EntityType entityType) {
        if (labelNames == null || labelNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Label> labels = labelNames.stream().map(name -> Label.builder()
                .name(name)
                .entityId(entityId)
                .entityType(entityType)
                .build()).collect(Collectors.toList());
        return labelRepository.saveAll(labels);
    }
}
