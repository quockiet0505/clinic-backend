package com.clinic.specification.appointment;

import com.clinic.dto.appointment.AppointmentFilterRequest;
import com.clinic.entity.appointment.Appointment;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentSpecification {

    private AppointmentSpecification() {}

    public static Specification<Appointment> filterBy(AppointmentFilterRequest filter) {
        return Specification.where(BaseSpecification.<Appointment>notDeleted())
                .and(appointmentSearch(filter.getSearch()))
                .and(BaseSpecification.equalIfPresent("status", filter.getStatus()))
                .and(BaseSpecification.equalIfPresent("appointmentType", filter.getAppointmentType()))
                .and(doctorIdEquals(filter.getDoctorId()))
                .and(patientIdEquals(filter.getPatientId()))
                .and(sourceFilter(filter.getSource()))
                .and(serviceFilter(filter.getServiceType()))
                .and(appointmentDateFilter(filter.getFromDate(), filter.getToDate()))
                .and(tabFilter(filter.getTab()));
    }

    private static Specification<Appointment> appointmentSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate namePredicate = cb.like(cb.lower(root.get("patient").get("fullName")), pattern);
            Predicate phonePredicate = cb.like(root.get("patient").get("phone"), pattern);
            return cb.or(namePredicate, phonePredicate);
        };
    }

    private static Specification<Appointment> doctorIdEquals(Integer doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) return cb.conjunction();
            return cb.equal(root.get("mainDoctor").get("staffId"), doctorId);
        };
    }

    private static Specification<Appointment> patientIdEquals(Integer patientId) {
        return (root, query, cb) -> {
            if (patientId == null) return cb.conjunction();
            return cb.equal(root.get("patient").get("patientId"), patientId);
        };
    }

    private static Specification<Appointment> sourceFilter(String source) {
        return (root, query, cb) -> {
            if (source == null || "ALL".equals(source)) return cb.conjunction();
            return cb.equal(root.get("appointmentType"), source);
        };
    }

    private static Specification<Appointment> serviceFilter(String serviceType) {
        return (root, query, cb) -> {
            if (serviceType == null || "ALL".equals(serviceType)) return cb.conjunction();
            return cb.equal(root.get("service").get("serviceType"), com.clinic.common.enums.ServiceType.valueOf(serviceType));
        };
    }

    private static Specification<Appointment> appointmentDateFilter(String fromDateStr, String toDateStr) {
        return (root, query, cb) -> {
            LocalDate fromDate = FilterUtils.parseDate(fromDateStr);
            LocalDate toDate = FilterUtils.parseDate(toDateStr);
            List<Predicate> predicates = new ArrayList<>();
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("appointmentDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("appointmentDate"), toDate));
            }
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Appointment> tabFilter(String tab) {
        return (root, query, cb) -> {
            if (tab == null) return cb.conjunction();
            LocalDate today = LocalDate.now();
            if ("today".equals(tab)) {
                return cb.equal(root.get("appointmentDate"), today);
            }
            if ("upcoming".equals(tab)) {
                return cb.greaterThan(root.get("appointmentDate"), today);
            }
            return cb.conjunction();
        };
    }
}
