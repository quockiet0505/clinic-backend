package com.clinic.service.profile;

import com.clinic.dto.auth.ChangePasswordRequest;
import com.clinic.dto.profile.UpdateProfileRequest;
import com.clinic.dto.profile.UserProfileResponse;
import com.clinic.entity.auth.Account;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.patient.PatientVitalProfile;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.patient.PatientVitalProfileRepository;
import com.clinic.repository.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AccountRepository accountRepository;
    private final PatientRepository patientRepository;
    private final PatientVitalProfileRepository vitalProfileRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getMyProfile() {
        String email = getCurrentEmail();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        UserProfileResponse response = new UserProfileResponse();
        response.setAccountId(account.getAccountId());
        response.setEmail(account.getEmail());
        response.setCreatedAt(account.getCreatedAt());

        // Lấy role name
        String roleName = account.getRoles().stream()
                .findFirst()
                .map(role -> role.getRoleName() != null ? role.getRoleName() : role.getRoleCode())
                .orElse("UNKNOWN");
        response.setRoleName(roleName);

        // Lấy thông tin từ Patient hoặc Staff
        Patient patient = patientRepository.findByAccount_AccountId(account.getAccountId()).orElse(null);
        if (patient != null) {
            response.setStaffId(null);
            response.setFullName(patient.getFullName());
            response.setGender(patient.getGender());
            response.setDateOfBirth(patient.getDateOfBirth());
            response.setPhone(patient.getPhone());
            response.setAddress(patient.getAddress());
            response.setAvatarUrl(patient.getAvatarUrl());

            PatientVitalProfile vp = vitalProfileRepository.findById(patient.getPatientId()).orElse(null);
            if (vp != null) {
                response.setHeight(vp.getHeight());
                response.setWeight(vp.getWeight());
                response.setBloodPressure(vp.getBloodPressure());
                response.setPulse(vp.getPulse());
                response.setBloodType(vp.getBloodType());
                response.setAllergies(vp.getAllergies());
                response.setChronicDiseases(vp.getChronicDiseases());
                response.setMedicalHistory(vp.getMedicalHistory());
            }
        } else {
            Staff staff = staffRepository.findByAccount_AccountId(account.getAccountId()).orElse(null);
            if (staff != null) {
                response.setStaffId(staff.getStaffId());
                response.setFullName(staff.getFullName());
                response.setGender(staff.getGender());
                response.setDateOfBirth(staff.getDateOfBirth());
                response.setPhone(staff.getPhone());
                response.setAddress(staff.getAddress());
                response.setAvatarUrl(staff.getImageUrl());
            }
        }

        return response;
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        String email = getCurrentEmail();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Patient patient = patientRepository.findByAccount_AccountId(account.getAccountId()).orElse(null);
        if (patient != null) {
            patient.setFullName(request.getFullName());
            patient.setGender(request.getGender());
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setPhone(request.getPhone());
            patient.setAddress(request.getAddress());
            if (request.getAvatarUrl() != null) {
                patient.setAvatarUrl(request.getAvatarUrl());
            }
            patientRepository.save(patient);

            PatientVitalProfile vp = vitalProfileRepository.findById(patient.getPatientId()).orElse(null);
            if (vp == null) {
                vp = new PatientVitalProfile();
                vp.setPatient(patient);
            }
            vp.setHeight(request.getHeight());
            vp.setWeight(request.getWeight());
            vp.setBloodPressure(request.getBloodPressure());
            vp.setPulse(request.getPulse());
            vp.setBloodType(request.getBloodType());
            vp.setAllergies(request.getAllergies());
            vp.setChronicDiseases(request.getChronicDiseases());
            vp.setMedicalHistory(request.getMedicalHistory());
            vitalProfileRepository.save(vp);
        } else {
            Staff staff = staffRepository.findByAccount_AccountId(account.getAccountId()).orElse(null);
            if (staff != null) {
                staff.setFullName(request.getFullName());
                staff.setGender(request.getGender());
                staff.setDateOfBirth(request.getDateOfBirth());
                staff.setPhone(request.getPhone());
                staff.setAddress(request.getAddress());
                if (request.getAvatarUrl() != null) {
                    staff.setImageUrl(request.getAvatarUrl());
                }
                staffRepository.save(staff);
            }
        }

        return getMyProfile();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String email = getCurrentEmail();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    private String getCurrentEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}