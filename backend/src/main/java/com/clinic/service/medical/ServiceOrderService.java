package com.clinic.service.medical;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.ServiceOrderFilterRequest;
import com.clinic.dto.medical.ServiceOrderRequest;
import com.clinic.dto.medical.ServiceOrderResponse;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.ServiceOrderMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.medical.ServiceOrderSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceRepository serviceRepository;
    private final StaffRepository staffRepository;
    private final ServiceOrderMapper serviceOrderMapper;

    /**
     * Doctor orders a new medical service (e.g., Blood test, X-Ray).
     */
    @Transactional
    public ServiceOrderResponse create(ServiceOrderRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical record not found."));
        com.clinic.entity.medical.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found."));
        Staff orderedBy = staffRepository.findById(request.getOrderedById())
                .orElseThrow(() -> new RuntimeException("Staff member not found."));

        if (service.getServiceType() == com.clinic.common.enums.ServiceType.EXAM) {
            throw new RuntimeException("Lỗi: Không được phép chỉ định dịch vụ Khám bệnh trong bệnh án. Chỉ được chỉ định Xét nghiệm (LAB_TEST) hoặc Chẩn đoán hình ảnh (X_RAY, ULTRASOUND).");
        }

        ServiceOrder order = serviceOrderMapper.toEntity(request);
        order.setMedicalRecord(record);
        order.setService(service);
        order.setOrderedBy(orderedBy);
        order.setStatus(ServiceOrderStatus.ORDERED);

        order.setServiceOriginalFee(service.getOriginalPrice());
        order.setServiceDiscount(service.getDiscountAmount());
        order.setServiceFinalFee(service.getDiscountAmount() != null && service.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0 ? service.getDiscountAmount() : service.getOriginalPrice());

        return serviceOrderMapper.toResponse(serviceOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<ServiceOrderResponse> getAll(ServiceOrderFilterRequest filter) {
        Specification<ServiceOrder> spec = ServiceOrderSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<ServiceOrder> page = serviceOrderRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(serviceOrderMapper::toResponse));
    }

    /**
     * Retrieves all service orders.
     */
    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> getAll() {
        return serviceOrderRepository.findAll().stream()
                .map(serviceOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a service order.
     * Includes tracking for sample collection or sample rejection by the lab.
     */
    @Transactional
    public ServiceOrderResponse updateStatus(Integer orderId, ServiceOrderStatus newStatus, Integer actionStaffId, String rejectionReason) {
        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Service Order not found."));
        
        order.setStatus(newStatus);

        // If a lab technician is processing this request
        if (actionStaffId != null) {
            Staff staff = staffRepository.findById(actionStaffId)
                    .orElseThrow(() -> new RuntimeException("Action Staff not found."));
            
            // If the lab collects the sample or completes the test
            if (newStatus == ServiceOrderStatus.DONE || newStatus == ServiceOrderStatus.ORDERED) { 
                order.setSampleCollectedBy(staff);
                order.setSampleCollectedAt(LocalDateTime.now());
            } 
            // If the lab rejects the sample (e.g., sample is corrupted)
            else if (newStatus == ServiceOrderStatus.REJECTED) {
                order.setRejectionReason(rejectionReason);
            }
        }

        return serviceOrderMapper.toResponse(serviceOrderRepository.save(order));
    }
}