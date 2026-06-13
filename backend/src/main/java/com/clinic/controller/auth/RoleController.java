package com.clinic.controller.auth;

import com.clinic.dto.common.ApiResponse;
import com.clinic.entity.auth.Role;
import com.clinic.service.auth.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
        Role newRole = roleService.createRole(role);
        return ResponseEntity.ok(ApiResponse.success(newRole));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable Integer id, @RequestBody Role roleDetails) {
        Role updatedRole = roleService.updateRole(id, roleDetails);
        if (updatedRole != null) {
            return ResponseEntity.ok(ApiResponse.success(updatedRole));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
