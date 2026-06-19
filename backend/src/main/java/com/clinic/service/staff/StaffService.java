package com.clinic.service.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.StaffType;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.staff.StaffFilterRequest;
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
import com.clinic.repository.staff.StaffDoctorReviewRepository;
import com.clinic.specification.staff.StaffSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ExpertiseRepository expertiseRepository;
    private final StaffDoctorReviewRepository staffDoctorReviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;

    private StaffResponse mapToResponseWithRating(Staff staff) {
        StaffResponse response = staffMapper.toResponse(staff);
        if (staff.getStaffType() == StaffType.DOCTOR) {
            response.setRating(staffDoctorReviewRepository.getAverageRatingByDoctorId(staff.getStaffId()));
        }
        if (staff.getIsDeleted() != null && staff.getIsDeleted() == 0) {
            response.setIsActive(1);
        } else {
            response.setIsActive(0);
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
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required for new staff");
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
    public PageResponse<StaffResponse> getAll(StaffFilterRequest filter) {
        Specification<Staff> spec = StaffSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Staff> page = staffRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(this::mapToResponseWithRating));
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
        staff.setSpecialtyTreatment(request.getSpecialtyTreatment());
        staff.setImageUrl(request.getImageUrl());
        if (request.getIsFeatured() != null) {
            staff.setIsFeatured(request.getIsFeatured());
        }
        if (request.getFeaturedPriority() != null) {
            staff.setFeaturedPriority(request.getFeaturedPriority());
        }

        if (request.getExpertiseId() != null) {
            @SuppressWarnings("null")
            Expertise expertise = expertiseRepository.findById(request.getExpertiseId())
                    .orElseThrow(() -> new RuntimeException("Expertise not found"));
            staff.setExpertise(expertise);
        } else {
            staff.setExpertise(null);
        }

        if (staff.getAccount() != null && request.getEmail() != null && !request.getEmail().isBlank()) {
            staff.getAccount().setEmail(request.getEmail());
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
        StaffFilterRequest filter = new StaffFilterRequest();
        filter.setExpertiseId(expertiseId);
        filter.setStaffType(staffType);
        filter.setPage(0);
        filter.setSize(10_000);
        return getAll(filter).getContent();
    }
}