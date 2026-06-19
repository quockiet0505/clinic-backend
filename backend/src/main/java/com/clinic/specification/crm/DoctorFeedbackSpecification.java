package com.clinic.specification.crm;

import com.clinic.dto.crm.DoctorFeedbackFilterRequest;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DoctorFeedbackSpecification {

    private DoctorFeedbackSpecification() {}

    public static Specification<DoctorReview> filterBy(DoctorFeedbackFilterRequest filter) {
        return Specification.where(doctorPatientSearch(filter.getSearch()))
                .and(BaseSpecification.<DoctorReview>equalIfPresent("rating", filter.getRating()))
                .and(BaseSpecification.<DoctorReview>dateTimeFieldBetween(
                        "createdAt",
                        FilterUtils.parseDate(filter.getFromDate()),
                        FilterUtils.parseDate(filter.getToDate())));
    }

    private static Specification<DoctorReview> doctorPatientSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("patient").get("fullName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("doctor").get("fullName")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
