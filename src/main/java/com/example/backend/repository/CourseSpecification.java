package com.example.backend.repository;

import com.example.backend.constant.EntityType;
import com.example.backend.entity.Course;
import com.example.backend.entity.Label;
import com.example.backend.entity.Tag;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;

public final class CourseSpecification {

    public static Specification<Course> isPublished() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), "PUBLISHED");
    }

    public static Specification<Course> titleContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(search)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + search.toLowerCase() + "%");
        };
    }

    public static Specification<Course> hasTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            // 1. Create the subquery that targets the Tag entity
            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);
            var tagRoot = subquery.from(Tag.class);
            subquery.select(tagRoot.get("id")); // Select anything, we just care if a row exists

            Predicate courseIdPredicate = criteriaBuilder.equal(tagRoot.get("entityId"), root.get("id"));

            // 3. The predicate for the polymorphic type
            Predicate entityTypePredicate = criteriaBuilder.equal(tagRoot.get("entityType"), EntityType.COURSE);

            // 4. The predicate to filter tags by the provided list of names
            Predicate tagNamePredicate = tagRoot.get("name").in(tags);

            // 5. Combine the subquery's WHERE clauses
            subquery.where(courseIdPredicate, entityTypePredicate, tagNamePredicate);

            // 6. Return the final EXISTS clause for the main query
            return criteriaBuilder.exists(subquery);
        };
    }

    /**
     * Creates a subquery for labels, similar to the hasTags method.
     */
    public static Specification<Course> hasLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);
            var labelRoot = subquery.from(Label.class);
            subquery.select(labelRoot.get("id"));

            Predicate courseIdPredicate = criteriaBuilder.equal(labelRoot.get("entityId"), root.get("id"));
            Predicate entityTypePredicate = criteriaBuilder.equal(labelRoot.get("entityType"), EntityType.COURSE);
            Predicate labelNamePredicate = labelRoot.get("name").in(labels);

            subquery.where(courseIdPredicate, entityTypePredicate, labelNamePredicate);

            return criteriaBuilder.exists(subquery);
        };
    }
}
