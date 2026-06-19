package com.clinic.specification.medical;

import com.clinic.dto.medical.DoctorServicePriceFilterRequest;
import com.clinic.entity.medical.DoctorServicePrice;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DoctorServicePriceSpecification {

    private DoctorServicePriceSpecification() {}

    public static Specification<DoctorServicePrice> filterBy(DoctorServicePriceFilterRequest filter) {
        return priceSearch(filter.getSearch());
    }

    private static Specification<DoctorServicePrice> priceSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate doctorPredicate = cb.like(cb.lower(root.get("staff").get("fullName")), pattern);
            Predicate servicePredicate = cb.like(cb.lower(root.get("service").get("serviceName")), pattern);
            return cb.or(doctorPredicate, servicePredicate);
        };
    }
}
