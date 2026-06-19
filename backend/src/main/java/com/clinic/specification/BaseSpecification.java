// src/main/java/com/clinic/specification/BaseSpecification.java
package com.clinic.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaseSpecification {

    private BaseSpecification() {}

    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), 0);
    }

    public static <T> Specification<T> searchLike(String search, String... fields) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(cb.like(cb.lower(root.get(field)), pattern));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static <T> Specification<T> equalIfPresent(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) return cb.conjunction();
            return cb.equal(root.get(field), value);
        };
    }

    /** Lọc theo createdAt trong khoảng ngày [from, to] */
    public static <T> Specification<T> createdAtDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Lọc theo field DATE: fromDate <= day <= toDate (dùng cho leave_request) */
    public static <T> Specification<T> overlapSingleDay(String fromField, String toField, LocalDate day) {
        return (root, query, cb) -> {
            if (day == null) return cb.conjunction();
            return cb.and(
                    cb.lessThanOrEqualTo(root.get(fromField), day),
                    cb.greaterThanOrEqualTo(root.get(toField), day)
            );
        };
    }

    /** Lọc theo field DATE >= minDate */
    public static <T> Specification<T> dateFieldGte(String field, LocalDate minDate) {
        return (root, query, cb) -> {
            if (minDate == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get(field), minDate);
        };
    }

    /** Lọc theo field DATE trong khoảng [from, to] trên cùng một cột */
    public static <T> Specification<T> dateFieldBetween(String field, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(field), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(field), to));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Lọc theo field DATETIME trong khoảng ngày [from, to] (bao gồm cả ngày to) */
    public static <T> Specification<T> dateTimeFieldBetween(String field, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(field), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get(field), to.plusDays(1).atStartOfDay()));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
