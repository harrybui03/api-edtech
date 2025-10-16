package com.example.backend.dto.model;

import com.example.backend.constant.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchDto {
    private UUID id;
    private String title;
    private String description;
    private String slug;
    private String image;
    private String videoLink;
    private boolean paidBatch;
    private BigDecimal actualPrice;
    private BigDecimal sellingPrice;
    private String language;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BatchStatus status;
    private Integer maxCapacity;
    private List<TagDto> tag;
    private List<LabelDto> label;
}
