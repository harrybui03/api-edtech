package com.example.backend.mapper;

import com.example.backend.dto.model.LabelDto;
import com.example.backend.entity.Label;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LabelMapper {
    public static List<LabelDto> toLabelDtoList(List<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        }
        return labels.stream()
                .map(label -> new LabelDto(label.getId(), label.getName()))
                .collect(Collectors.toList());
    }
}
