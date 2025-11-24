package com.example.backend.service;

import com.example.backend.constant.EntityType;
import com.example.backend.entity.Label;
import com.example.backend.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class LabelService {
    private final LabelRepository labelRepository;

    @Transactional
    public List<Label> upsertLabels(List<String> labelNames, UUID entityId, EntityType entityType) {
        // If the incoming list is null or empty, delete all existing labels for the entity.
        if (labelNames == null || labelNames.isEmpty()) {
            labelRepository.deleteByEntityIdAndEntityType(entityId, entityType);
            return Collections.emptyList();
        }

        List<Label> existingLabels = labelRepository.findByEntityIdAndEntityType(entityId, entityType);
        Set<String> existingLabelNames = existingLabels.stream()
                .map(Label::getName)
                .collect(Collectors.toSet());

        Set<String> newLabelNames = labelNames.stream().collect(Collectors.toSet());

        // Labels to add
        List<String> labelsToAdd = newLabelNames.stream()
                .filter(name -> !existingLabelNames.contains(name))
                .collect(Collectors.toList());

        // Labels to remove
        List<String> labelsToRemove = existingLabelNames.stream()
                .filter(name -> !newLabelNames.contains(name))
                .collect(Collectors.toList());

        if (!labelsToRemove.isEmpty()) {
            labelRepository.deleteByEntityIdAndEntityTypeAndNameIn(entityId, entityType, labelsToRemove);
        }

        if (!labelsToAdd.isEmpty()) {
            List<Label> newLabels = labelsToAdd.stream().map(name -> Label.builder()
                    .name(name)
                    .entityId(entityId)
                    .entityType(entityType).build()).collect(Collectors.toList());
            labelRepository.saveAll(newLabels);
        }
        return labelRepository.findByEntityIdAndEntityType(entityId, entityType);
    }
}
