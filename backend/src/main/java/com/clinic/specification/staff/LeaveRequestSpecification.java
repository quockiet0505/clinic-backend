package com.clinic.specification.staff;

import com.clinic.common.enums.LeaveStatus;
import com.clinic.dto.staff.LeaveRequestFilterRequest;
import com.clinic.entity.staff.LeaveRequest;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestSpecification {

    private LeaveRequestSpecification() {}

    public static Specification<LeaveRequest> filterBy(LeaveRequestFilterRequest filter) {
        return Specification.where(leaveSearch(filter.getSearch()))
                .and(BaseSpecification.equalIfPresent("leaveType", filter.getLeaveType()))
                .and(staffTypeEquals(filter.getStaffType()))
                .and(tabFilter(filter));
    }

    private static Specification<LeaveRequest> leaveSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("staff").get("fullName")), pattern));
            predicates.add(cb.like(cb.lower(root.get("reason")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<LeaveRequest> staffTypeEquals(com.clinic.common.enums.StaffType staffType) {
        return (root, query, cb) -> {
            if (staffType == null) return cb.conjunction();
            return cb.equal(root.get("staff").get("staffType"), staffType);
        };
    }

    private static Specification<LeaveRequest> tabFilter(LeaveRequestFilterRequest filter) {
        return (root, query, cb) -> {
            String tab = filter.getTab() != null ? filter.getTab().toLowerCase() : null;
            LocalDate fromDate = FilterUtils.parseDate(filter.getFromDate());
            LocalDate toDate = FilterUtils.parseDate(filter.getToDate());

            if ("today".equals(tab)) {
                LocalDate today = LocalDate.now();
                return cb.and(
                        cb.equal(root.get("status"), LeaveStatus.APPROVED),
                        cb.lessThanOrEqualTo(root.get("fromDate"), today),
                        cb.greaterThanOrEqualTo(root.get("toDate"), today)
                );
            }
            if ("pending".equals(tab)) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("status"), LeaveStatus.PENDING));
                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), fromDate));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            }
            if ("processed".equals(tab)) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(root.get("status").in(LeaveStatus.APPROVED, LeaveStatus.REJECTED));
                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), fromDate));
                }
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            List<Predicate> predicates = new ArrayList<>();
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (fromDate != null && toDate != null && fromDate.equals(toDate)) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fromDate"), fromDate));
                predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), fromDate));
            } else {
                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("fromDate"), fromDate));
                }
                if (toDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("fromDate"), toDate));
                }
            }
            if (predicates.isEmpty()) return cb.conjunction();
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
