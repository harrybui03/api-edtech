package com.example.backend.mapper;

import com.example.backend.dto.model.TagDto;
import com.example.backend.entity.Tag;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TagMapper {
    public static List<TagDto> toTagDtoList(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags.stream()
                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }
}
