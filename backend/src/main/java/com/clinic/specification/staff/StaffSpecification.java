package com.clinic.specification.staff;

import com.clinic.dto.staff.StaffFilterRequest;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.entity.staff.Staff;
import com.clinic.specification.BaseSpecification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class StaffSpecification {

    private StaffSpecification() {}

    public static Specification<Staff> filterBy(StaffFilterRequest filter) {
        return Specification.where(BaseSpecification.<Staff>notDeleted())
                .and(staffSearch(filter.getSearch()))
                .and(BaseSpecification.equalIfPresent("staffType", filter.getStaffType()))
                .and(expertiseIdEquals(filter.getExpertiseId()))
                .and(isActiveEquals(filter.getIsActive()))
                .and(minRatingGte(filter.getMinRating()));
    }

    private static Specification<Staff> staffSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            var accountJoin = root.join("account", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("fullName")), pattern));
            predicates.add(cb.like(root.get("phone"), pattern));
            predicates.add(cb.like(cb.lower(accountJoin.get("email")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Staff> expertiseIdEquals(Integer expertiseId) {
        return (root, query, cb) -> {
            if (expertiseId == null) return cb.conjunction();
            return cb.equal(root.get("expertise").get("expertiseId"), expertiseId);
        };
    }

    private static Specification<Staff> isActiveEquals(Integer isActive) {
        return (root, query, cb) -> {
            if (isActive == null) return cb.conjunction();
            return cb.equal(root.get("account").get("isActive"), isActive);
        };
    }

    private static Specification<Staff> minRatingGte(Integer minRating) {
        return (root, query, cb) -> {
            if (minRating == null) return cb.conjunction();
            Subquery<Double> ratingSubquery = query.subquery(Double.class);
            Root<DoctorReview> reviewRoot = ratingSubquery.from(DoctorReview.class);
            ratingSubquery.select(cb.coalesce(cb.avg(reviewRoot.get("rating").as(Double.class)), 0.0));
            ratingSubquery.where(cb.equal(reviewRoot.get("doctor").get("staffId"), root.get("staffId")));
            return cb.greaterThanOrEqualTo(ratingSubquery, minRating.doubleValue());
        };
    }
}
