package com.clinic.service.auth;

import com.clinic.dto.auth.RoleFilterRequest;
import com.clinic.dto.common.PageResponse;
import com.clinic.entity.auth.Role;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.specification.auth.RoleSpecification;
import com.clinic.util.FilterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public PageResponse<Role> getAll(RoleFilterRequest filter) {
        Specification<Role> spec = RoleSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Role> page = roleRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Integer id, Role roleDetails) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role != null) {
            role.setRoleName(roleDetails.getRoleName());
            role.setRoleCode(roleDetails.getRoleCode());
            return roleRepository.save(role);
        }
        return null;
    }

    public void deleteRole(Integer id) {
        roleRepository.deleteById(id);
    }
}
