package com.clinic.specification.medical;

import com.clinic.dto.medical.ServiceResultFilterRequest;
import com.clinic.entity.medical.ServiceResult;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceResultSpecification {

    private ServiceResultSpecification() {}

    public static Specification<ServiceResult> filterBy(ServiceResultFilterRequest filter) {
        return Specification.where(resultSearch(filter.getSearch()))
                .and(orderStatusEquals(filter.getStatus()))
                .and(enteredAtBetween(filter.getFromDate(), filter.getToDate()));
    }

    private static Specification<ServiceResult> resultSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(
                    cb.lower(root.get("serviceOrder").get("medicalRecord").get("patient").get("fullName")), pattern));
            predicates.add(cb.like(
                    cb.lower(root.get("serviceOrder").get("service").get("serviceName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("conclusion")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<ServiceResult> orderStatusEquals(
            com.clinic.common.enums.ServiceOrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("serviceOrder").get("status"), status);
        };
    }

    private static Specification<ServiceResult> enteredAtBetween(String fromDateStr, String toDateStr) {
        return (root, query, cb) -> {
            LocalDate fromDate = FilterUtils.parseDate(fromDateStr);
            LocalDate toDate = FilterUtils.parseDate(toDateStr);
            List<Predicate> predicates = new ArrayList<>();
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("enteredAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(cb.lessThan(root.get("enteredAt"), toDate.plusDays(1).atStartOfDay()));
            }
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
