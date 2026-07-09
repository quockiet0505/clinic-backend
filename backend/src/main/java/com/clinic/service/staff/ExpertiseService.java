package com.clinic.service.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.StaffType;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.staff.ExpertiseFilterRequest;
import com.clinic.dto.staff.ExpertiseRequest;
import com.clinic.dto.staff.ExpertiseResponse;
import com.clinic.entity.staff.Expertise;
import com.clinic.mapper.staff.ExpertiseMapper;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.staff.ExpertiseSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpertiseService {
    private final ExpertiseRepository expertiseRepository;
    private final ExpertiseMapper expertiseMapper;
    private final StaffRepository staffRepository;

    @Transactional
    public ExpertiseResponse create(ExpertiseRequest request) {
        Expertise expertise = expertiseMapper.toEntity(request);
        return expertiseMapper.toResponse(expertiseRepository.save(expertise));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpertiseResponse> getAll(ExpertiseFilterRequest filter) {
        Specification<Expertise> spec = ExpertiseSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Expertise> page = expertiseRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(expertise -> {
            ExpertiseResponse response = expertiseMapper.toResponse(expertise);
            long doctorCount = staffRepository.countByExpertiseAndStaffType(expertise, StaffType.DOCTOR);
            long technicianCount = staffRepository.countByExpertiseAndStaffType(expertise, StaffType.LAB_TECH);
            response.setDoctorCount((int) doctorCount);
            response.setTechnicianCount((int) technicianCount);
            return response;
        }));
    }

    @Transactional(readOnly = true)
    public List<ExpertiseResponse> getAllLegacy() {
        return expertiseRepository.findAll().stream()
            .map(expertise -> {
                ExpertiseResponse response = expertiseMapper.toResponse(expertise);
                long doctorCount = staffRepository.countByExpertiseAndStaffType(expertise, StaffType.DOCTOR);
                long technicianCount = staffRepository.countByExpertiseAndStaffType(expertise, StaffType.LAB_TECH);
                response.setDoctorCount((int) doctorCount);
                response.setTechnicianCount((int) technicianCount);
                return response;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public ExpertiseResponse update(Integer id, ExpertiseRequest request) {
        Expertise expertise = expertiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expertise not found"));
        expertise.setExpertiseName(request.getExpertiseName());
        return expertiseMapper.toResponse(expertiseRepository.save(expertise));
    }

    @Transactional
    public void delete(Integer id) {
        if (!expertiseRepository.existsById(id)) {
            throw new RuntimeException("Expertise not found");
        }
        expertiseRepository.deleteById(id);
    }
}
