package com.example.backend.service;

import com.example.backend.constant.BatchStatus;
import com.example.backend.constant.EntityType;
import com.example.backend.constant.UserRoleEnum;
import com.example.backend.dto.model.BatchDto;
import com.example.backend.dto.model.LabelDto;
import com.example.backend.dto.model.TagDto;
import com.example.backend.dto.request.batch.CreateBatchRequest;
import com.example.backend.dto.request.batch.UpdateBatchRequest;
import com.example.backend.entity.*;
import com.example.backend.excecption.ForbiddenException;
import com.example.backend.excecption.InvalidRequestDataException;
import com.example.backend.excecption.ResourceNotFoundException;
import com.example.backend.mapper.BatchMapper;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.backend.util.SlugConverter.toSlug;

@Service
@RequiredArgsConstructor
public class BatchService {
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final BatchInstructorRepository batchInstructorRepository;
    private final BatchMapper batchMapper;
    private final TagService tagService;
    private final LabelService labelService;
    private final TagRepository tagRepository;
    private final LabelRepository labelRepository;


    @Transactional
    public BatchDto createBatch(CreateBatchRequest request) {
        Batch batch = batchMapper.toEntity(request);
        batch.setSlug(generateUniqueSlug(request.getTitle()));

        Batch savedBatch = batchRepository.save(batch);
        BatchInstructor batchInstructor = new BatchInstructor();
        batchInstructor.setInstructor(getCurrentUser());
        batchInstructor.setBatch(savedBatch);
        batchInstructorRepository.save(batchInstructor);
        List<Tag> tags = tagService.upsertTags(request.getTags().stream().map(TagDto::getName).collect(Collectors.toList()), savedBatch.getId(), EntityType.BATCH);
        List<Label> labels = labelService.upsertLabels(request.getLabels().stream().map(LabelDto::getName).collect(Collectors.toList()), savedBatch.getId(), EntityType.BATCH);

        return batchMapper.toDto(savedBatch , tags , labels);
    }

    @Transactional
    public BatchDto updateBatch(UUID id, UpdateBatchRequest request) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        checkCourseOwnership(batch);

        if (request.getTitle() != null && !request.getTitle().equals(batch.getTitle())) {
            batch.setSlug(generateUniqueSlug(request.getTitle()));
        }

        List<Tag> tags = tagService.upsertTags(request.getTags().stream().map(TagDto::getName).collect(Collectors.toList()), batch.getId(), EntityType.BATCH);
        List<Label> labels = labelService.upsertLabels(request.getLabels().stream().map(LabelDto::getName).collect(Collectors.toList()), batch.getId(), EntityType.BATCH);

        batchMapper.updateEntityFromRequest(request, batch);

        Batch updatedBatch = batchRepository.save(batch);
        return batchMapper.toDto(updatedBatch, tags , labels);
    }

    @Transactional
    public void deleteBatch(UUID id) {
        Batch batch = findBatchById(id);
        checkCourseOwnership(batch);
        if(batch.getStatus().equals(BatchStatus.PUBLISHED)){
            throw  new ForbiddenException("Batch is already published");   
        }

        tagRepository.deleteByEntityIdAndEntityType(id, EntityType.BATCH);
        labelRepository.deleteByEntityIdAndEntityType(id, EntityType.BATCH);
        batchRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<BatchDto> getPublishedBatches(Pageable pageable, List<String> tags, List<String> labels, String search) {
        Specification<Batch> spec = BatchSpecification.isPublished();
        spec = spec.and(BatchSpecification.titleContains(search))
                .and(BatchSpecification.hasLabels(labels))
                .and(BatchSpecification.hasTags(tags));
        Page<Batch> batchPage = batchRepository.findAll(spec, pageable);
        return getBatchDtos(batchPage);
    }

    @Transactional(readOnly = true)
    public Page<BatchDto> getMyBatches(Pageable pageable, BatchStatus status) {
        User currentUser = getCurrentUser();
        Page<Batch> batchPage = batchRepository.findBatchesByInstructorAndStatus(currentUser.getId(), status, pageable);
        return getBatchDtos(batchPage);
    }

    @Transactional(readOnly = true)
    public BatchDto getBatchBySlug(String slug) {
        Batch batch = batchRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with slug: " + slug));
        List<Tag> tags = tagRepository.findByEntityIdAndEntityType(batch.getId(), EntityType.BATCH);
        List<Label> labels = labelRepository.findByEntityIdAndEntityType(batch.getId(), EntityType.BATCH);
        return batchMapper.toDto(batch, tags, labels);
    }

    @Transactional(readOnly = true)
    public BatchDto getBatchById(UUID batchId) {
        Batch batch = findBatchById(batchId);
        checkCourseOwnership(batch);

        List<Tag> tags = tagRepository.findByEntityIdAndEntityType(batch.getId(), EntityType.BATCH);
        List<Label> labels = labelRepository.findByEntityIdAndEntityType(batch.getId(), EntityType.BATCH);
        return batchMapper.toDto(batch, tags, labels);
    }

    @Transactional
    public void publishBatch(UUID batchId) {
        Batch batch = findBatchById(batchId);
        checkCourseOwnership(batch);

        if (batch.getStatus().equals(BatchStatus.PUBLISHED)) {
            throw new InvalidRequestDataException("Batch with id " + batchId + " is already published.");
        }

        batch.setStatus(BatchStatus.PUBLISHED);
        batchRepository.save(batch);
    }

    @Transactional
    public void addInstructorsToBatch(UUID batchId, List<UUID> newInstructorIds) {
        Batch batch = findBatchById(batchId);
        checkCourseOwnership(batch);

        List<User> newInstructors = userRepository.findAllById(newInstructorIds);
        if (newInstructors.size() != newInstructorIds.size()) {
            throw new ResourceNotFoundException("One or more instructors not found.");
        }

        for (User newInstructorUser : newInstructors) {
            boolean isCourseCreator = newInstructorUser.getRoles().stream()
                    .anyMatch(userRole -> userRole.getRole() == UserRoleEnum.COURSE_CREATOR);

            if (!isCourseCreator) {
                throw new ForbiddenException("User " + newInstructorUser.getEmail() + " must have the 'COURSE_CREATOR' role to be added as an instructor.");
            }

            boolean alreadyExists = batch.getInstructors().stream()
                    .anyMatch(instructor -> instructor.getInstructor().getId().equals(newInstructorUser.getId()));

            if (alreadyExists) {
                throw new InvalidRequestDataException("User " + newInstructorUser.getEmail() + " is already an instructor for this batch.");
            }

            BatchInstructor newBatchInstructor = new BatchInstructor();
            newBatchInstructor.setBatch(batch);
            newBatchInstructor.setInstructor(newInstructorUser);
            batchInstructorRepository.save(newBatchInstructor);
        }
    }

    @Transactional
    public void removeInstructorFromBatch(UUID batchId, UUID instructorIdToRemove) {
        Batch batch = findBatchById(batchId);
        checkCourseOwnership(batch);

        if (batch.getInstructors().size() <= 1) {
            throw new ForbiddenException("Cannot remove the last instructor from a batch.");
        }

        BatchInstructor instructorToRemove = batch.getInstructors().stream()
                .filter(ci -> ci.getInstructor().getId().equals(instructorIdToRemove))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Instructor with id " + instructorIdToRemove + " not found on this batch."));

        batch.getInstructors().remove(instructorToRemove);
        batchInstructorRepository.delete(instructorToRemove);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = toSlug(title);
        String slug = baseSlug;
        int counter = 1;
        while (batchRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }

    private Batch findBatchById(UUID batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));
    }

    private void checkCourseOwnership(Batch batch) {
        User currentUser = getCurrentUser();

        boolean isOwner = batch.getInstructors().stream()
                .anyMatch(instructor -> instructor.getInstructor().getId().equals(currentUser.getId()));

        if (!isOwner) {
            throw new ForbiddenException("You are not an instructor for this batch and cannot modify it.");
        }
    }

    private Page<BatchDto> getBatchDtos(Page<Batch> batchPage) {
        return batchPage.map(batch -> {
            List<Tag> tags = tagRepository.findByEntityIdAndEntityType(batch.getId(), EntityType.BATCH);
            List<Label> labels = labelRepository.findByEntityIdAndEntityType(batch.getId(), EntityType.BATCH);
            return batchMapper.toDto(batch, tags, labels);
        });
    }
}
