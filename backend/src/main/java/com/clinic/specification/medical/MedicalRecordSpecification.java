package com.clinic.specification.medical;

import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.dto.medical.MedicalRecordFilterRequest;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordSpecification {

    private MedicalRecordSpecification() {}

    public static Specification<MedicalRecord> filterBy(MedicalRecordFilterRequest filter) {
        return Specification.where(medicalRecordSearch(filter.getSearch()))
                .and(BaseSpecification.equalIfPresent("status", filter.getStatus()))
                .and(doctorIdEquals(filter.getDoctorId()))
                .and(tabFilter(filter.getTab()))
                .and(createdAtFilter(filter.getFromDate(), filter.getToDate()));
    }

    private static Specification<MedicalRecord> medicalRecordSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("patient").get("fullName")), pattern);
        };
    }

    private static Specification<MedicalRecord> doctorIdEquals(Integer doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) return cb.conjunction();
            return cb.equal(root.get("mainDoctor").get("staffId"), doctorId);
        };
    }

    private static Specification<MedicalRecord> tabFilter(String tab) {
        return (root, query, cb) -> {
            if (tab == null) return cb.conjunction();
            if ("active".equals(tab)) {
                return root.get("status").in(
                        MedicalRecordStatus.PENDING,
                        MedicalRecordStatus.IN_PROGRESS,
                        MedicalRecordStatus.WAITING_RESULT
                );
            }
            if ("archived".equals(tab)) {
                return root.get("status").in(MedicalRecordStatus.DONE, MedicalRecordStatus.CANCELLED);
            }
            return cb.conjunction();
        };
    }

    private static Specification<MedicalRecord> createdAtFilter(String fromDateStr, String toDateStr) {
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
