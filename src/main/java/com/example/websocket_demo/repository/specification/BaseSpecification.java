package com.example.websocket_demo.repository.specification;

import com.example.websocket_demo.entity.ChatRoomEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public class BaseSpecification<T> {
    public Specification<T> alwaysTrue() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public Specification<T> equalField(String field, Object value) {
        return (root, query, criteriaBuilder) ->
                value != null ? criteriaBuilder.equal(root.get(field), value) : criteriaBuilder.conjunction();
    }

    public Specification<T> likeField(String field, String value) {
        return (root, query, criteriaBuilder) ->
                (value != null && !value.isEmpty()) ?
                        criteriaBuilder.like(root.get(field), "%" + value + "%") : criteriaBuilder.conjunction();
    }

    public Specification<T> inField(String field, Iterable<?> values) {
        return (root, query, criteriaBuilder) ->
                (values != null && values.iterator().hasNext()) ?
                        root.get(field).in(values) : criteriaBuilder.conjunction();
    }

    public Specification<T> isNull(String key) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isNull(root.get(key));
    }
}
