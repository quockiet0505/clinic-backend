package com.clinic.specification.crm;

import com.clinic.dto.crm.FollowUpFilterRequest;
import com.clinic.entity.crm.FollowUp;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FollowUpSpecification {

    private FollowUpSpecification() {}

    public static Specification<FollowUp> filterBy(FollowUpFilterRequest filter) {
        return Specification.where(patientSearch(filter.getSearch()))
                .and(BaseSpecification.<FollowUp>equalIfPresent("status", filter.getStatus()))
                .and(BaseSpecification.<FollowUp>dateTimeFieldBetween(
                        "scheduledDatetime",
                        FilterUtils.parseDate(filter.getFromDate()),
                        FilterUtils.parseDate(filter.getToDate())))
                .and(tabFilter(filter.getTab()));
    }

    private static Specification<FollowUp> patientSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("patient").get("fullName")), pattern));
            predicates.add(cb.like(root.get("patient").get("phone"), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<FollowUp> tabFilter(String tab) {
        return (root, query, cb) -> {
            if (tab == null) {
                return cb.conjunction();
            }
            LocalDate today = LocalDate.now();
            if ("today".equals(tab)) {
                LocalDateTime startOfDay = today.atStartOfDay();
                LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("scheduledDatetime"), startOfDay),
                        cb.lessThan(root.get("scheduledDatetime"), startOfNextDay));
            }
            if ("upcoming".equals(tab)) {
                return cb.greaterThanOrEqualTo(
                        root.get("scheduledDatetime"), today.plusDays(1).atStartOfDay());
            }
            return cb.conjunction();
        };
    }
}
