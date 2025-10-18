package com.example.websocket_demo.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.Date;

@NoRepositoryBean
public class BaseSpecification<T> {
    public static <T> Specification<T> alwaysTrue() {
        return (root, query, builder) -> builder.equal(builder.literal(1), 1);
    }

    public static <T> Specification<T> alwaysFalse() {
        return (root, query, builder) -> builder.notEqual(builder.literal(1), 1);
    }

    public static <T> Specification<T> likeField(String key, String value) {
        return !value.isBlank() ? (root, query, builder) -> builder.like(builder.upper(root.get(key)), "%" + likeSpecialToStr(value.toUpperCase()) + "%", KEY_ESCAPE) : null;
    }

    public static <T> Specification<T> inField(String key, Collection<?> values) {
        return values != null && !values.isEmpty() ? (root, query, builder) -> root.get(key).in(values) : null;
    }

    public <V extends Number> Specification<T> fromTo(String key, V from, V to) {
        if (from == null || to == null)
            return null;
        return (root, query, builder) -> builder.ge(root.get(key), from);
    }

    public static <T> Specification<T> startTime(String key, Date startTime) {
        if (startTime == null)
            return null;
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get(key), startTime);
    }

    public static <T> Specification<T> endTime(String key, Date endTime) {
        if (endTime == null)
            return null;
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get(key), endTime);
    }

    public static <T> Specification<T> equalIgnoreCase(String key, String value) {
        return (root, query, builder) -> builder.equal(builder.upper(root.get(key)), value.toUpperCase());
    }

    public static <T> Specification<T> equal(String key, Object value) {
        if (value == null)
            return null;
        return (root, query, builder) -> builder.equal(root.get(key), value);
    }

    public static <T> Specification<T> isNull(String key) {
        return (root, query, builder) -> builder.isNull(root.get(key));
    }

    public static <T> Specification<T> isNotNull(String key) {
        return (root, query, builder) -> builder.isNotNull(root.get(key));
    }

    public static final char KEY_ESCAPE = '\\';

    public static String likeSpecialToStr(String str) {
        str = str.replaceAll("_", KEY_ESCAPE + "_");
        str = str.replaceAll("%", KEY_ESCAPE + "%");
        return str;
    }
}
