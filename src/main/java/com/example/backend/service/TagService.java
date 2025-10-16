package com.example.backend.service;

import com.example.backend.constant.EntityType;
import com.example.backend.entity.Tag;
import com.example.backend.repository.TagRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class TagService {
    private final TagRepository tagRepository;


    public List<Tag> upsertTags(List<String> tagNames, UUID entityId , EntityType entityType) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Tag> tags = tagNames.stream().map(name -> Tag.builder()
                .name(name)
                .entityId(entityId)
                .entityType(entityType)
                .build()).collect(Collectors.toList());
        return tagRepository.saveAll(tags);
    }
}
