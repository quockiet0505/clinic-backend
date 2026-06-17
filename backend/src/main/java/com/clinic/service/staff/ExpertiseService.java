package com.clinic.service.staff;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.StaffType;
import com.clinic.dto.staff.ExpertiseRequest;
import com.clinic.dto.staff.ExpertiseResponse;
import com.clinic.entity.staff.Expertise;
import com.clinic.mapper.staff.ExpertiseMapper;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpertiseService {
    private final ExpertiseRepository expertiseRepository;
    private final ExpertiseMapper expertiseMapper;
    private final StaffRepository staffRepository; // thêm repository này

    @Transactional
    public ExpertiseResponse create(ExpertiseRequest request) {
        Expertise expertise = expertiseMapper.toEntity(request);
        return expertiseMapper.toResponse(expertiseRepository.save(expertise));
    }

    // CHỈ GIỮ LẠI MỘT METHOD getAll() có tính doctorCount
    @Transactional(readOnly = true)
    public List<ExpertiseResponse> getAll() {
        return expertiseRepository.findAll().stream()
            .map(expertise -> {
                ExpertiseResponse response = expertiseMapper.toResponse(expertise);
                long doctorCount = staffRepository.countByExpertiseAndStaffType(expertise, StaffType.DOCTOR);
                response.setDoctorCount((int) doctorCount);
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