package com.clinic.specification.patient;

import com.clinic.dto.patient.PatientFilterRequest;
import com.clinic.entity.patient.Patient;
import com.clinic.specification.BaseSpecification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PatientSpecification {

    private PatientSpecification() {}

    public static Specification<Patient> filterBy(PatientFilterRequest filter) {
        return Specification.where(BaseSpecification.<Patient>notDeleted())
                .and(patientSearch(filter.getSearch()))
                .and(genderEquals(filter.getGender()));
    }

    private static Specification<Patient> patientSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("fullName")), pattern));
            predicates.add(cb.like(root.get("phone"), pattern));
            predicates.add(cb.like(cb.lower(root.get("address")), pattern));
            predicates.add(cb.like(root.get("patientId").as(String.class), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Patient> genderEquals(String gender) {
        return (root, query, cb) -> {
            if (gender == null || "ALL".equalsIgnoreCase(gender)) return cb.conjunction();
            return cb.equal(root.get("gender"), gender);
        };
    }
}
