package com.clinic.service.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.StaffType;
import com.clinic.dto.staff.StaffRequest;
import com.clinic.dto.staff.StaffResponse;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.staff.StaffMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.DoctorReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ExpertiseRepository expertiseRepository;
    private final DoctorReviewRepository doctorReviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;

    private StaffResponse mapToResponseWithRating(Staff staff) {
        StaffResponse response = staffMapper.toResponse(staff);
        if (staff.getStaffType() == StaffType.DOCTOR) {
            response.setRating(doctorReviewRepository.getAverageRatingByDoctorId(staff.getStaffId()));
        }
        return response;
    }

    @SuppressWarnings("null")
    @Transactional
    public StaffResponse create(StaffRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Password is required for new staff");
        }

        // 1. Create Account
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        String roleCode = "ROLE_" + request.getStaffType().name();
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role " + roleCode + " not found"));
        account.getRoles().add(role);

        // 2. Create Staff Profile
        Staff staff = staffMapper.toEntity(request);
        staff.setAccount(account);

        if (request.getExpertiseId() != null) {
            Expertise expertise = expertiseRepository.findById(request.getExpertiseId())
                    .orElseThrow(() -> new RuntimeException("Expertise not found"));
            staff.setExpertise(expertise);
        }

        return mapToResponseWithRating(staffRepository.save(staff));
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getAllActive() {
        return staffRepository.findByIsDeleted(0).stream()
                .map(this::mapToResponseWithRating)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffResponse update(@NonNull Integer id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        staff.setFullName(request.getFullName());
        staff.setGender(request.getGender());
        staff.setDateOfBirth(request.getDateOfBirth());
        staff.setPhone(request.getPhone());
        staff.setAddress(request.getAddress());
        staff.setStaffType(request.getStaffType());
        staff.setExperience(request.getExperience());
        staff.setImageUrl(request.getImageUrl());

        if (request.getExpertiseId() != null) {
            @SuppressWarnings("null")
            Expertise expertise = expertiseRepository.findById(request.getExpertiseId())
                    .orElseThrow(() -> new RuntimeException("Expertise not found"));
            staff.setExpertise(expertise);
        } else {
            staff.setExpertise(null);
        }

        return mapToResponseWithRating(staffRepository.save(staff));
    }

    @Transactional
    public void softDelete(@NonNull Integer id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        staff.setIsDeleted(1);
        
        if (staff.getAccount() != null) {
            staff.getAccount().setIsActive(0); // Deactivate login
        }
        staffRepository.save(staff);
    }

    public List<StaffResponse> getAllDoctors() {
        return staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0)
                .stream()
                .map(this::mapToResponseWithRating)
                .collect(Collectors.toList());
    }

    public List<StaffResponse> getFeaturedDoctors() {
        return staffRepository.findByStaffTypeAndIsDeletedAndIsFeaturedOrderByFeaturedPriorityAsc(StaffType.DOCTOR, 0, true)
                .stream()
                .map(this::mapToResponseWithRating)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffResponse getById(Integer id) {

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        return mapToResponseWithRating(staff);
    }
    
    @Transactional(readOnly = true)
    public List<StaffResponse> getAll(
            Integer expertiseId,
            StaffType staffType
    ) {
    
        List<Staff> staffs = staffRepository.findByIsDeleted(0);
    
        if (staffType != null) {
            staffs = staffs.stream()
                    .filter(s -> s.getStaffType() == staffType)
                    .toList();
        }
    
        if (expertiseId != null) {
            staffs = staffs.stream()
                    .filter(s ->
                            s.getExpertise() != null &&
                            s.getExpertise().getExpertiseId().equals(expertiseId))
                    .toList();
        }
    
        return staffs.stream()
                .map(this::mapToResponseWithRating)
                .toList();
    }
}