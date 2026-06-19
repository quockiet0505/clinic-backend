package com.clinic.specification.auth;

import com.clinic.dto.auth.RoleFilterRequest;
import com.clinic.entity.auth.Role;
import com.clinic.specification.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification {

    private RoleSpecification() {}

    public static Specification<Role> filterBy(RoleFilterRequest filter) {
        return Specification.where(
                BaseSpecification.<Role>searchLike(filter.getSearch(), "roleName", "roleCode"));
    }
}
