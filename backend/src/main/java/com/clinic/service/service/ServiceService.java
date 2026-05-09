package com.clinic.service.service;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.service.ServiceRequest;
import com.clinic.dto.service.ServiceResponse;
import com.clinic.entity.service.Service;
import com.clinic.mapper.service.ServiceMapper;
import com.clinic.repository.service.ServiceRepository;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        Service service = serviceMapper.toEntity(request);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllActive() {
        return serviceRepository.findByIsDeleted(0).stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponse update(Integer id, ServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setServiceName(request.getServiceName());
        service.setServiceType(request.getServiceType());
        service.setPrice(request.getPrice());
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional
    public void softDelete(Integer id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setIsDeleted(1);
        serviceRepository.save(service);
    }
}