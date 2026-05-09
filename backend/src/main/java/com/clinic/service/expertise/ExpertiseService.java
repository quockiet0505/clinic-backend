package com.clinic.service.expertise;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.expertise.ExpertiseRequest;
import com.clinic.dto.expertise.ExpertiseResponse;
import com.clinic.entity.staff.Expertise;
import com.clinic.mapper.expertise.ExpertiseMapper;
import com.clinic.repository.staff.ExpertiseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpertiseService {
    private final ExpertiseRepository expertiseRepository;
    private final ExpertiseMapper expertiseMapper;

    @Transactional
    public ExpertiseResponse create(ExpertiseRequest request) {
        Expertise expertise = expertiseMapper.toEntity(request);
        return expertiseMapper.toResponse(expertiseRepository.save(expertise));
    }

    @Transactional(readOnly = true)
    public List<ExpertiseResponse> getAll() {
        return expertiseRepository.findAll().stream()
                .map(expertiseMapper::toResponse)
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