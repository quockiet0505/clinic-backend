package com.clinic.specification.staff;

import com.clinic.dto.staff.StaffFilterRequest;
import com.clinic.entity.crm.DoctorReview;
import com.clinic.entity.staff.Staff;
import com.clinic.specification.BaseSpecification;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import com.clinic.entity.medical.DoctorServicePrice;
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
                .and(expertiseNameEquals(filter.getExpertiseName()))
                .and(isActiveEquals(filter.getIsActive()))
                .and(minRatingGte(filter.getMinRating()))
                .and(genderEquals(filter.getGender()))
                .and(priceBetween(filter.getMinPrice(), filter.getMaxPrice()));
    }

    private static Specification<Staff> staffSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            var accountJoin = root.join("account", JoinType.LEFT);
            var expertiseJoin = root.join("expertise", JoinType.LEFT);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("fullName")), pattern));
            predicates.add(cb.like(root.get("phone"), pattern));
            predicates.add(cb.like(cb.lower(accountJoin.get("email")), pattern));
            predicates.add(cb.like(cb.lower(expertiseJoin.get("expertiseName")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Staff> expertiseIdEquals(Integer expertiseId) {
        return (root, query, cb) -> {
            if (expertiseId == null) return cb.conjunction();
            return cb.equal(root.get("expertise").get("expertiseId"), expertiseId);
        };
    }

    private static Specification<Staff> expertiseNameEquals(String expertiseName) {
        return (root, query, cb) -> {
            if (expertiseName == null || expertiseName.isEmpty()) return cb.conjunction();
            return cb.equal(root.get("expertise").get("expertiseName"), expertiseName);
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

    private static Specification<Staff> genderEquals(String gender) {
        return (root, query, cb) -> {
            if (gender == null || gender.isEmpty()) return cb.conjunction();
            return cb.equal(root.get("gender"), gender);
        };
    }

    private static Specification<Staff> priceBetween(java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return cb.conjunction();
            Subquery<java.math.BigDecimal> priceSubquery = query.subquery(java.math.BigDecimal.class);
            Root<DoctorServicePrice> priceRoot = priceSubquery.from(DoctorServicePrice.class);
            
            var actualPrice = cb.selectCase()
                .when(cb.and(cb.isNotNull(priceRoot.get("discountAmount")), cb.greaterThan(priceRoot.get("discountAmount"), java.math.BigDecimal.ZERO)), priceRoot.get("discountAmount"))
                .otherwise(priceRoot.get("originalPrice")).as(java.math.BigDecimal.class);
                
            priceSubquery.select(actualPrice);
            priceSubquery.where(cb.equal(priceRoot.get("staff").get("staffId"), root.get("staffId")));

            List<Predicate> predicates = new ArrayList<>();
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(priceSubquery, minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(priceSubquery, maxPrice));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
