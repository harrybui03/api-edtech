package com.example.backend.repository;

import com.example.backend.constant.BatchStatus;
import com.example.backend.constant.EntityType;
import com.example.backend.entity.Batch;
import com.example.backend.entity.Label;
import com.example.backend.entity.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;

public final class BatchSpecification {

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
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);
            var tagRoot = subquery.from(Tag.class);
            subquery.select(tagRoot.get("id"));

            Predicate batchIdPredicate = criteriaBuilder.equal(tagRoot.get("entityId"), root.get("id"));
            Predicate entityTypePredicate = criteriaBuilder.equal(tagRoot.get("entityType"), EntityType.BATCH);
            Predicate tagNamePredicate = tagRoot.get("name").in(tagNames);

            subquery.where(batchIdPredicate, entityTypePredicate, tagNamePredicate);

            return criteriaBuilder.exists(subquery);
        };
    }

    public static Specification<Batch> hasLabels(List<String> labelNames) {
        if (labelNames == null || labelNames.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);
            var labelRoot = subquery.from(Label.class);
            subquery.select(labelRoot.get("id"));

            Predicate batchIdPredicate = criteriaBuilder.equal(labelRoot.get("entityId"), root.get("id"));
            Predicate entityTypePredicate = criteriaBuilder.equal(labelRoot.get("entityType"), EntityType.BATCH);
            Predicate labelNamePredicate = labelRoot.get("name").in(labelNames);

            subquery.where(batchIdPredicate, entityTypePredicate, labelNamePredicate);

            return criteriaBuilder.exists(subquery);
        };
    }
}