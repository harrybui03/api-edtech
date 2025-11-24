package com.example.backend.dto.request.batch;

import com.example.backend.constant.BatchStatus;
import com.example.backend.dto.model.LabelDto;
import com.example.backend.dto.model.TagDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchRequest {
    private String title;
    private String description;
    private String image;
    private String videoLink;
    private boolean paidBatch;
    private BigDecimal actualPrice;
    private BigDecimal sellingPrice;
    private BigDecimal amountUsd;
    private String currency;
    private String language;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BatchStatus status;
    private Integer maxCapacity;
    private List<TagDto> tags;
    private List<LabelDto> labels;
}
