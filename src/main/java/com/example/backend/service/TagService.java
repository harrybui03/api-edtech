package com.example.backend.service;

import com.example.backend.constant.EntityType;
import com.example.backend.entity.Tag;
import com.example.backend.repository.TagRepository;
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
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public List<Tag> upsertTags(List<String> tagNames, UUID entityId, EntityType entityType) {
        // If the incoming list is null or empty, delete all existing tags for the entity.
        if (tagNames == null || tagNames.isEmpty()) {
            tagRepository.deleteByEntityIdAndEntityType(entityId, entityType);
            return Collections.emptyList();
        }

        List<Tag> existingTags = tagRepository.findByEntityIdAndEntityType(entityId, entityType);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<String> newTagNames = tagNames.stream().collect(Collectors.toSet());

        // Tags to add are the ones in the new list but not in the existing list.
        List<String> tagsToAdd = newTagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .collect(Collectors.toList());

        // Tags to remove are the ones in the existing list but not in the new list.
        List<String> tagsToRemove = existingTagNames.stream()
                .filter(name -> !newTagNames.contains(name))
                .collect(Collectors.toList());

        if (!tagsToRemove.isEmpty()) {
            tagRepository.deleteByEntityIdAndEntityTypeAndNameIn(entityId, entityType, tagsToRemove);
        }

        if (!tagsToAdd.isEmpty()) {
            List<Tag> newTags = tagsToAdd.stream().map(name -> Tag.builder()
                    .name(name)
                    .entityId(entityId)
                    .entityType(entityType).build()).collect(Collectors.toList());
            tagRepository.saveAll(newTags);
        }
        return tagRepository.findByEntityIdAndEntityType(entityId, entityType);
    }
}
