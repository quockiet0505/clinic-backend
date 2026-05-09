package com.clinic.controller.medical;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.dto.medical.MedicalRecordVitalRequest;
import com.clinic.dto.medical.MedicalRecordVitalResponse;
import com.clinic.service.medical.MedicalRecordVitalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/medical-vitals")
@RequiredArgsConstructor
public class MedicalRecordVitalController {
    
    private final MedicalRecordVitalService vitalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'DOCTOR', 'ADMIN')") // Nurses usually input vitals
    public ResponseEntity<MedicalRecordVitalResponse> saveOrUpdate(@Valid @RequestBody MedicalRecordVitalRequest request) {
        return ResponseEntity.ok(vitalService.saveOrUpdate(request));
    }

    @GetMapping("/record/{recordId}")
    @PreAuthorize("hasAnyRole('STAFF', 'DOCTOR', 'ADMIN', 'PATIENT')")
    public ResponseEntity<MedicalRecordVitalResponse> getByRecordId(@PathVariable Integer recordId) {
        return ResponseEntity.ok(vitalService.getByRecordId(recordId));
    }
}