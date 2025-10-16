package com.example.backend.repository;

import com.example.backend.constant.BatchStatus;
import com.example.backend.entity.Batch;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public class BatchSpecification {

    public static Specification<Batch> isPublished() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), BatchStatus.PUBLISHED);
    }

    public static Specification<Batch> titleContains(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchTerm.toLowerCase() + "%");
        };
    }

    public static Specification<Batch> hasTags(List<String> tagNames) {
        return (root, query, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(tagNames)) return criteriaBuilder.conjunction();
            return root.join("tags").get("name").in(tagNames);
        };
    }

    public static Specification<Batch> hasLabels(List<String> labelNames) {
        return (root, query, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(labelNames)) return criteriaBuilder.conjunction();
            return root.join("labels").get("name").in(labelNames);
        };
    }
}