package com.clinic.specification.crm;

import com.clinic.dto.crm.ClinicFeedbackFilterRequest;
import com.clinic.entity.crm.Feedback;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ClinicFeedbackSpecification {

    private ClinicFeedbackSpecification() {}

    public static Specification<Feedback> filterBy(ClinicFeedbackFilterRequest filter) {
        return Specification.where(patientNameSearch(filter.getSearch()))
                .and(BaseSpecification.<Feedback>equalIfPresent("rating", filter.getRating()))
                .and(BaseSpecification.<Feedback>dateTimeFieldBetween(
                        "createdAt",
                        FilterUtils.parseDate(filter.getFromDate()),
                        FilterUtils.parseDate(filter.getToDate())));
    }

    private static Specification<Feedback> patientNameSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate namePredicate = cb.like(
                    cb.lower(root.get("medicalRecord").get("patient").get("fullName")), pattern);
            return namePredicate;
        };
    }
}
