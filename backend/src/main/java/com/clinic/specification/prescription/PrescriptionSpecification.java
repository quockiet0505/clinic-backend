package com.clinic.specification.prescription;

import com.clinic.dto.prescription.PrescriptionFilterRequest;
import com.clinic.entity.prescription.Prescription;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PrescriptionSpecification {

    private PrescriptionSpecification() {}

    public static Specification<Prescription> filterBy(PrescriptionFilterRequest filter) {
        return Specification.where(prescriptionSearch(filter.getSearch()))
                .and(statusEquals(filter.getStatus()))
                .and(BaseSpecification.<Prescription>dateTimeFieldBetween(
                        "createdAt",
                        FilterUtils.parseDate(filter.getFromDate()),
                        FilterUtils.parseDate(filter.getToDate())));
    }

    private static Specification<Prescription> prescriptionSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate patientName = cb.like(
                    cb.lower(root.get("medicalRecord").get("patient").get("fullName")), pattern);
            try {
                Integer id = Integer.parseInt(search.trim());
                return cb.or(patientName, cb.equal(root.get("prescriptionId"), id));
            } catch (NumberFormatException e) {
                return patientName;
            }
        };
    }

    private static Specification<Prescription> statusEquals(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isEmpty() || "ALL".equals(status)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }
}
