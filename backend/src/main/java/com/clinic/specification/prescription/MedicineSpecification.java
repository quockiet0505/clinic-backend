package com.clinic.specification.prescription;

import com.clinic.dto.prescription.MedicineFilterRequest;
import com.clinic.entity.prescription.Medicine;
import com.clinic.specification.BaseSpecification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MedicineSpecification {

    private MedicineSpecification() {}

    public static Specification<Medicine> filterBy(MedicineFilterRequest filter) {
        return Specification.where(BaseSpecification.<Medicine>notDeleted())
                .and(medicineSearch(filter.getSearch()));
    }

    private static Specification<Medicine> medicineSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            predicates.add(cb.like(cb.lower(root.get("activeElement")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
