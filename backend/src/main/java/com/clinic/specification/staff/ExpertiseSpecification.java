package com.clinic.specification.staff;

import com.clinic.dto.staff.ExpertiseFilterRequest;
import com.clinic.entity.staff.Expertise;
import com.clinic.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class ExpertiseSpecification {

    private ExpertiseSpecification() {}

    public static Specification<Expertise> filterBy(ExpertiseFilterRequest filter) {
        return Specification.where(
                BaseSpecification.<Expertise>searchLike(filter.getSearch(), "expertiseName"));
    }
}
