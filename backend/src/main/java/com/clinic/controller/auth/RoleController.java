package com.clinic.controller.auth;

import com.clinic.dto.auth.RoleFilterRequest;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.entity.auth.Role;
import com.clinic.service.auth.RoleService;
import com.clinic.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Role>>> getAll(@ModelAttribute RoleFilterRequest filter) {
        return ResponseUtil.success("Roles retrieved successfully", roleService.getAll(filter));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Role>>> getAllLegacy() {
        return ResponseUtil.success("Roles retrieved successfully", roleService.getAllRoles());
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
