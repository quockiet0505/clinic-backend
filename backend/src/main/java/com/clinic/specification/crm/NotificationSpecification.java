package com.clinic.specification.crm;

import com.clinic.dto.crm.NotificationFilterRequest;
import com.clinic.entity.crm.Notification;
import com.clinic.specification.BaseSpecification;
import com.clinic.util.FilterUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NotificationSpecification {

    private NotificationSpecification() {}

    public static Specification<Notification> filterBy(NotificationFilterRequest filter) {
        return Specification.where(notificationSearch(filter.getSearch()))
                .and(typeEquals(filter.getType()))
                .and(BaseSpecification.<Notification>dateTimeFieldBetween(
                        "sentAt",
                        FilterUtils.parseDate(filter.getFromDate()),
                        FilterUtils.parseDate(filter.getToDate())));
    }

    private static Specification<Notification> notificationSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("account").get("email")), pattern));
            predicates.add(cb.like(cb.lower(root.get("content")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Notification> typeEquals(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isEmpty() || "ALL".equals(type)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), Notification.Type.valueOf(type));
        };
    }
}
