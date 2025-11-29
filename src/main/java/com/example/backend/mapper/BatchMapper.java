package com.example.backend.mapper;

import com.example.backend.constant.BatchStatus;
import com.example.backend.dto.model.BatchDto;
import com.example.backend.dto.model.InstructorDto;
import com.example.backend.dto.request.batch.CreateBatchRequest;
import com.example.backend.dto.request.batch.UpdateBatchRequest;
import com.example.backend.entity.Batch;
import com.example.backend.entity.Label;
import com.example.backend.entity.Tag;
import com.example.backend.entity.BatchInstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class BatchMapper {

    /**
     * Converts a Batch entity to a BatchDto.
     *
     * @param batch The Batch entity to convert.
     * @return The corresponding BatchDto.
     */

    public BatchDto toDto(Batch batch , List<Tag> tags, List<Label> labels) {
        if (batch == null) {
            return null;
        }

        BatchDto dto = new BatchDto();
        dto.setImage(batch.getImage());
        dto.setId(batch.getId());
        dto.setTitle(batch.getTitle());
        dto.setDescription(batch.getDescription());
        dto.setSlug(batch.getSlug());
        dto.setTags(TagMapper.toTagDtoList(tags));
        dto.setLabels(LabelMapper.toLabelDtoList(labels));
        dto.setVideoLink(batch.getVideoLink());
        dto.setPaidBatch(batch.isPaidBatch());
        dto.setActualPrice(batch.getActualPrice());
        dto.setSellingPrice(batch.getSellingPrice());
        dto.setLanguage(batch.getLanguage());
        dto.setStartTime(batch.getStartTime());
        dto.setEndTime(batch.getEndTime());
        dto.setStatus(batch.getStatus());
        dto.setMaxCapacity(batch.getMaxCapacity());
        dto.setCurrency(batch.getCurrency());
        dto.setAmountUsd(batch.getAmountUsd());
        dto.setOpenTime(batch.getOpenTime());
        dto.setCloseTime(batch.getCloseTime());

        if (batch.getInstructors() != null) {
            dto.setInstructors(batch.getInstructors().stream()
                    .map(this::toInstructorDto)
                    .toList());
        }
        return dto;
    }

    private InstructorDto toInstructorDto(BatchInstructor batchInstructor) {
        if (batchInstructor == null || batchInstructor.getInstructor() == null) {
            return null;
        }
        return new InstructorDto(
                batchInstructor.getInstructor().getId(),
                batchInstructor.getInstructor().getFullName(),
                batchInstructor.getInstructor().getEmail(),
                batchInstructor.getInstructor().getUserImage()
        );
    }

    /**
     * Converts a CreateBatchRequest to a new Batch entity.
     *
     * @param request The creation request.
     * @return A new Batch entity.
     */
    public Batch toEntity(CreateBatchRequest request) {
        if (request == null) {
            return null;
        }
        Batch batch = new Batch();
        batch.setTitle(request.getTitle());
        batch.setDescription(request.getDescription());
        batch.setImage(request.getImage());
        batch.setVideoLink(request.getVideoLink());
        batch.setPaidBatch(request.isPaidBatch());
        batch.setActualPrice(request.getActualPrice());
        batch.setSellingPrice(request.getSellingPrice());
        batch.setLanguage(request.getLanguage());
        batch.setStartTime(request.getStartTime());
        batch.setEndTime(request.getEndTime());
        batch.setStatus(request.getStatus() != null ? request.getStatus() : BatchStatus.DRAFT);
        batch.setMaxCapacity(request.getMaxCapacity());
        batch.setCurrency(request.getCurrency());
        batch.setAmountUsd(request.getAmountUsd());
        return batch;
    }

    /**
     * Updates an existing Batch entity from an UpdateBatchRequest.
     * Only non-null fields in the request are used for the update.
     *
     * @param request The update request.
     * @param batch   The entity to update.
     */
    public void updateEntityFromRequest(UpdateBatchRequest request, Batch batch) {
        if (request == null || batch == null) {
            return;
        }
        if (request.getTitle() != null) {
            batch.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            batch.setDescription(request.getDescription());
        }
        if (request.getImage() != null) {
            batch.setImage(request.getImage());
        }
        if (request.getVideoLink() != null) {
            batch.setVideoLink(request.getVideoLink());
        }
        if (request.getPaidBatch() != null) {
            batch.setPaidBatch(request.getPaidBatch());
        }
        if (request.getActualPrice() != null) {
            batch.setActualPrice(request.getActualPrice());
        }
        if (request.getSellingPrice() != null) {
            batch.setSellingPrice(request.getSellingPrice());
        }
        if (request.getLanguage() != null) {
            batch.setLanguage(request.getLanguage());
        }
        if (request.getStartTime() != null) {
            batch.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            batch.setEndTime(request.getEndTime());
        }
        if (request.getStatus() != null) {
            batch.setStatus(request.getStatus());
        }
        if (request.getMaxCapacity() != null) {
            batch.setMaxCapacity(request.getMaxCapacity());
        }
        if (request.getCurrency() != null) {
            batch.setCurrency(request.getCurrency());
        }
        if (request.getAmountUsd() != null) {
            batch.setAmountUsd(request.getAmountUsd());
        }
        if (request.getOpenTime() != null) {
            batch.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            batch.setCloseTime(request.getCloseTime());
        }
    }
}