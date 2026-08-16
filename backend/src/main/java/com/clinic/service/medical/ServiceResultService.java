package com.clinic.service.medical;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.ServiceResultFilterRequest;
import com.clinic.dto.medical.ServiceResultRequest;
import com.clinic.dto.medical.ServiceResultResponse;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.medical.ServiceResult;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.medical.ServiceResultMapper;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceResultRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.service.appointment.AppointmentQueueService;
import com.clinic.specification.medical.ServiceResultSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceResultService {
    private final ServiceResultRepository resultRepository;
    private final ServiceOrderRepository orderRepository;
    private final StaffRepository staffRepository;
    private final ServiceResultMapper resultMapper;
    private final com.clinic.repository.patient.PatientRepository patientRepository;
    private final com.clinic.service.crm.NotificationService notificationService;
    private final AppointmentQueueService appointmentQueueService;

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
        result.setAttachmentUrls(request.getAttachmentUrls());
        result.setEnteredBy(labTech);

        // Auto-update Order Status
        order.setStatus(ServiceOrderStatus.DONE);
        orderRepository.save(order);

        // Notify Doctor
        if (order.getMedicalRecord() != null && order.getMedicalRecord().getMainDoctor() != null
                && order.getMedicalRecord().getMainDoctor().getAccount() != null) {
            notificationService.createAndSendNotification(
                    order.getMedicalRecord().getMainDoctor().getAccount().getAccountId(),
                    "Đã có kết quả cận lâm sàng cho bệnh nhân " + order.getMedicalRecord().getPatient().getFullName() + " (Mã Order: " + order.getOrderId() + ").",
                    "SYSTEM"
            );
        }

        // Notify Patient
        if (order.getMedicalRecord() != null && order.getMedicalRecord().getPatient() != null
                && order.getMedicalRecord().getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    order.getMedicalRecord().getPatient().getAccount().getAccountId(),
                    "Kết quả cận lâm sàng " + order.getService().getServiceName() + " của bạn đã sẵn sàng. Vui lòng quay lại phòng khám.",
                    "SYSTEM"
            );
        }

        ServiceResultResponse response = resultMapper.toResponse(resultRepository.save(result));

        if (order.getMedicalRecord() != null) {
            appointmentQueueService.tryAutoReturnFromLab(order.getMedicalRecord().getRecordId());
        }

        return response;
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
    public PageResponse<ServiceResultResponse> getAll(ServiceResultFilterRequest filter) {
        Specification<ServiceResult> spec = ServiceResultSpecification.filterBy(filter);
        Pageable pageable = buildPageable(filter);
        Page<ServiceResult> page = resultRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(resultMapper::toResponse));
    }

    private Pageable buildPageable(ServiceResultFilterRequest filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "enteredAt";
        if ("createdAt".equals(sortBy)) {
            sortBy = "enteredAt";
        }
        filter.setSortBy(sortBy);
        return FilterUtils.buildPageable(filter);
    }

    @Transactional(readOnly = true)
    public java.util.List<ServiceResultResponse> getAll() {
        return resultRepository.findAll()
                .stream()
                .map(resultMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}