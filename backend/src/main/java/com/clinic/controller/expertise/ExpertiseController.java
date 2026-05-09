package com.clinic.controller.expertise;

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

import com.clinic.dto.expertise.ExpertiseRequest;
import com.clinic.dto.expertise.ExpertiseResponse;
import com.clinic.service.expertise.ExpertiseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/expertises")
@RequiredArgsConstructor
public class ExpertiseController {
    private final ExpertiseService expertiseService;

    @GetMapping
    public ResponseEntity<List<ExpertiseResponse>> getAll() {
        return ResponseEntity.ok(expertiseService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExpertiseResponse> create(@Valid @RequestBody ExpertiseRequest request) {
        return ResponseEntity.ok(expertiseService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExpertiseResponse> update(@PathVariable Integer id, @Valid @RequestBody ExpertiseRequest request) {
        return ResponseEntity.ok(expertiseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        expertiseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}