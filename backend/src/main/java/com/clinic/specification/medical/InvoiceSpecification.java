package com.clinic.specification.medical;

import com.clinic.dto.medical.InvoiceFilterRequest;
import com.clinic.entity.medical.Invoice;
import com.clinic.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class InvoiceSpecification {

    private InvoiceSpecification() {}

    public static Specification<Invoice> filterBy(InvoiceFilterRequest filter) {
        Specification<Invoice> spec = Specification.where(null);
        if (filter.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        return spec.and(patientIdEquals(filter.getPatientId()))
                .and(patientNameLike(filter.getPatientName()))
                .and(invoiceSearch(filter.getSearch()));
    }

    private static Specification<Invoice> patientIdEquals(Integer patientId) {
        return (root, query, cb) -> {
            if (patientId == null) return cb.conjunction();
            return cb.equal(root.get("patient").get("patientId"), patientId);
        };
    }

    private static Specification<Invoice> patientNameLike(String patientName) {
        return (root, query, cb) -> {
            if (patientName == null || patientName.isEmpty()) return cb.conjunction();
            String pattern = "%" + patientName.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("patient").get("fullName")), pattern);
        };
    }

    private static Specification<Invoice> invoiceSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("patient").get("fullName")), pattern),
                    cb.like(cb.lower(root.get("patient").get("phone")), pattern)
            );
        };
    }
}
