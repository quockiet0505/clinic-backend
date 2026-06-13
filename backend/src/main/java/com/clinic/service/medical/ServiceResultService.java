package com.clinic.service.medical;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.medical.ServiceResultRequest;
import com.clinic.dto.medical.ServiceResultResponse;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.medical.ServiceResult;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.ServiceResultMapper;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceResultRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceResultService {
    private final ServiceResultRepository resultRepository;
    private final ServiceOrderRepository orderRepository;
    private final StaffRepository staffRepository;
    private final ServiceResultMapper resultMapper;
    private final com.clinic.repository.patient.PatientRepository patientRepository;

    @Transactional
    public ServiceResultResponse submitResult(ServiceResultRequest request) {
        ServiceOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Service Order not found"));
        
        if (order.getStatus() == ServiceOrderStatus.DONE) {
            throw new RuntimeException("Result has already been submitted for this order");
        }

        Staff labTech = staffRepository.findById(request.getEnteredById())
                .orElseThrow(() -> new RuntimeException("Lab Technician not found"));

        ServiceResult result = new ServiceResult();
        result.setServiceOrder(order);
        result.setResultData(request.getResultData());
        result.setConclusion(request.getConclusion());
        result.setEnteredBy(labTech);

        // Auto-update Order Status
        order.setStatus(ServiceOrderStatus.DONE);
        orderRepository.save(order);

        return resultMapper.toResponse(resultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public ServiceResultResponse getByOrderId(Integer orderId) {
        ServiceResult result = resultRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Result not found for this order"));
        return resultMapper.toResponse(result);
    }

    @Transactional(readOnly = true)
    public java.util.List<ServiceResultResponse> getMyResults(String email) {
        com.clinic.entity.patient.Patient patient = patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
                
        return resultRepository.findByServiceOrder_MedicalRecord_Patient_PatientId(patient.getPatientId())
                .stream()
                .map(resultMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.List<ServiceResultResponse> getAll() {
        return resultRepository.findAll()
                .stream()
                .map(resultMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}