package com.clinic.service.auth;

import com.clinic.entity.auth.Role;
import com.clinic.repository.auth.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

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
