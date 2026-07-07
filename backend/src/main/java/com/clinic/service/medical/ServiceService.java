package com.clinic.service.medical;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.ServiceType;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.ServiceFilterRequest;
import com.clinic.dto.medical.ServiceRequest;
import com.clinic.dto.medical.ServiceResponse;
import com.clinic.entity.medical.Service;
import com.clinic.mapper.medical.ServiceMapper;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.specification.medical.ServiceSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getAll(ServiceFilterRequest filter) {
        Specification<Service> spec = ServiceSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Service> page = serviceRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(serviceMapper::toResponse));
    }

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        if (request.getServiceType() != null && request.getServiceType().isHiddenEverywhere()) {
            throw new RuntimeException("Dịch vụ khám tổng quát (EXAM) hiện không được phép tạo.");
        }
        Service service = serviceMapper.toEntity(request);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllActive(Boolean bookableOnly) {
        return serviceRepository.findByIsDeleted(0).stream()
                .filter(s -> !s.getServiceType().isHiddenEverywhere())
                .filter(s -> !Boolean.TRUE.equals(bookableOnly) || s.getServiceType().isPatientBookable())
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponse update(Integer id, ServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setServiceName(request.getServiceName());
        if (request.getServiceType() != null && request.getServiceType().isHiddenEverywhere()) {
            throw new RuntimeException("Dịch vụ khám tổng quát (EXAM) hiện không được phép cập nhật.");
        }
        service.setServiceType(request.getServiceType());
        service.setOriginalPrice(request.getOriginalPrice());
        service.setDiscountAmount(request.getDiscountPrice());
        service.setImageUrl(request.getImageUrl());
        service.setIsFeatured(request.getIsFeatured());
        service.setFeaturedPriority(request.getFeaturedPriority());
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getFeaturedServices(Boolean bookableOnly) {
        return serviceRepository.findByIsDeletedAndIsFeaturedOrderByFeaturedPriorityAsc(0, true).stream()
                .filter(s -> !s.getServiceType().isHiddenEverywhere())
                .filter(s -> !Boolean.TRUE.equals(bookableOnly) || s.getServiceType().isPatientBookable())
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void softDelete(Integer id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setIsDeleted(1);
        serviceRepository.save(service);
    }
}
