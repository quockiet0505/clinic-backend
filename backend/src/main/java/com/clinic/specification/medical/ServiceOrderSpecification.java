package com.clinic.specification.medical;

import com.clinic.dto.medical.ServiceOrderFilterRequest;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrderSpecification {

    private ServiceOrderSpecification() {}

    public static Specification<ServiceOrder> filterBy(ServiceOrderFilterRequest filter) {
        return Specification.where(serviceOrderSearch(filter.getSearch()))
                .and(BaseSpecification.equalIfPresent("status", filter.getStatus()))
                .and(createdAtFilter(filter.getFromDate(), filter.getToDate()));
    }

    private static Specification<ServiceOrder> serviceOrderSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate patientName = cb.like(cb.lower(root.get("medicalRecord").get("patient").get("fullName")), pattern);
            Predicate serviceName = cb.like(cb.lower(root.get("service").get("serviceName")), pattern);
            return cb.or(patientName, serviceName);
        };
    }

    private static Specification<ServiceOrder> createdAtFilter(String fromDateStr, String toDateStr) {
        return (root, query, cb) -> {
            LocalDate fromDate = FilterUtils.parseDate(fromDateStr);
            LocalDate toDate = FilterUtils.parseDate(toDateStr);
            List<Predicate> predicates = new ArrayList<>();
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), toDate.plusDays(1).atStartOfDay()));
            }
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
