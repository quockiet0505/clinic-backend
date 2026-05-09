package com.clinic.service.medical;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.medical.ServiceOrderRequest;
import com.clinic.dto.medical.ServiceOrderResponse;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.Service;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.ServiceOrderMapper;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceOrderService {
    private final ServiceOrderRepository orderRepository;
    private final MedicalRecordRepository recordRepository;
    private final ServiceRepository serviceRepository;
    private final StaffRepository staffRepository;
    private final ServiceOrderMapper orderMapper;

    @Transactional
    public ServiceOrderResponse create(ServiceOrderRequest request) {
        MedicalRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical Record not found"));
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Staff doctor = staffRepository.findById(request.getOrderedById())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        ServiceOrder order = new ServiceOrder();
        order.setMedicalRecord(record);
        order.setService(service);
        order.setOrderedBy(doctor);
        order.setStatus(ServiceOrderStatus.ORDERED);

        // Auto-update Medical Record status
        record.setStatus(MedicalRecordStatus.WAITING_RESULT);
        recordRepository.save(record);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> getByRecordId(Integer recordId) {
        return orderRepository.findByMedicalRecord_RecordId(recordId).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
}