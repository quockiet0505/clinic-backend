package com.clinic.controller.medicine;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.medicine.MedicineRequest;
import com.clinic.dto.medicine.MedicineResponse;
import com.clinic.service.medicine.MedicineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')") // Doctors need to see meds to prescribe
    public ResponseEntity<List<MedicineResponse>> getAll() {
        return ResponseEntity.ok(medicineService.getAllActive());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // Only Admins/Staff manage inventory
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<MedicineResponse> update(@PathVariable Integer id, @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Deletion is strict
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        medicineService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}