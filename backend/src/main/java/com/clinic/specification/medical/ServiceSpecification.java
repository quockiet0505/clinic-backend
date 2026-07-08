package com.clinic.specification.medical;

import com.clinic.dto.medical.ServiceFilterRequest;
import com.clinic.entity.medical.Service;
import com.clinic.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class ServiceSpecification {

    private ServiceSpecification() {}

    public static Specification<Service> filterBy(ServiceFilterRequest filter) {
        return Specification.where(BaseSpecification.<Service>notDeleted())
                .and((root, query, cb) -> cb.notEqual(root.get("serviceType"), com.clinic.common.enums.ServiceType.EXAM))
                .and(BaseSpecification.searchLike(filter.getSearch(), "serviceName"))
                .and(BaseSpecification.equalIfPresent("serviceType", filter.getServiceType()));
    }
}
