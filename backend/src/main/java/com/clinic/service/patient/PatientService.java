package com.clinic.service.patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.patient.PatientRequest;
import com.clinic.dto.patient.PatientResponse;
import com.clinic.entity.auth.Account;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.patient.PatientVitalProfile;
import com.clinic.mapper.patient.PatientMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.patient.PatientVitalProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientVitalProfileRepository vitalRepository;
    private final AccountRepository accountRepository;
    private final PatientMapper patientMapper;

    // Internal Validation replacing the Validator folder
    private void validatePatientData(PatientRequest request) {
        if (request.getPhone() != null && !request.getPhone().matches("\\d{10,15}")) {
            throw new RuntimeException("Invalid phone number format");
        }
        if (request.getDateOfBirth() != null && request.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new RuntimeException("Date of birth cannot be in the future");
        }
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        validatePatientData(request);
        Patient patient = patientMapper.toEntity(request);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            patient.setAccount(account);
        }

        Patient savedPatient = patientRepository.save(patient);

        PatientVitalProfile vitalProfile = new PatientVitalProfile();
        vitalProfile.setPatient(savedPatient);
        vitalProfile.setHeight(request.getHeight());
        vitalProfile.setBloodType(request.getBloodType());
        vitalProfile.setAllergies(request.getAllergies());
        vitalProfile.setMedicalHistory(request.getMedicalHistory());
        vitalProfile.setUpdatedAt(LocalDateTime.now());
        
        PatientVitalProfile savedVital = vitalRepository.save(vitalProfile);
        return patientMapper.toResponse(savedPatient, savedVital);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllActive() {
        return patientRepository.findByIsDeleted(0).stream()
                .map(patient -> {
                    PatientVitalProfile vp = vitalRepository.findById(patient.getPatientId()).orElse(null);
                    return patientMapper.toResponse(patient, vp);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        if (patient.getIsDeleted() == 1) {
            throw new RuntimeException("Patient has been deleted");
        }
        PatientVitalProfile vp = vitalRepository.findById(patient.getPatientId()).orElse(null);
        return patientMapper.toResponse(patient, vp);
    }

    @Transactional
    public PatientResponse update(Integer id, PatientRequest request) {
        validatePatientData(request);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        patient.setFullName(request.getFullName());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());

        Patient savedPatient = patientRepository.save(patient);

        PatientVitalProfile vitalProfile = vitalRepository.findById(id)
                .orElse(new PatientVitalProfile());
        
        vitalProfile.setPatient(savedPatient);
        vitalProfile.setHeight(request.getHeight());
        vitalProfile.setBloodType(request.getBloodType());
        vitalProfile.setAllergies(request.getAllergies());
        vitalProfile.setMedicalHistory(request.getMedicalHistory());
        vitalProfile.setUpdatedAt(LocalDateTime.now());
        
        PatientVitalProfile savedVital = vitalRepository.save(vitalProfile);

        return patientMapper.toResponse(savedPatient, savedVital);
    }

    @Transactional
    public void softDelete(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        patient.setIsDeleted(1);
        patientRepository.save(patient);
    }
}