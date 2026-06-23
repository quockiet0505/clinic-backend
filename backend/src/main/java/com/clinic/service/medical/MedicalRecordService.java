package com.clinic.service.medical;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.dto.medical.MedicalRecordDetailResponse;
import com.clinic.dto.medical.MedicalRecordFilterRequest;
import com.clinic.dto.medical.MedicalRecordRequest;
import com.clinic.dto.medical.MedicalRecordResponse;
import com.clinic.dto.medical.ServiceOrderResponse;
import com.clinic.dto.prescription.PrescriptionResponse;
import com.clinic.entity.auth.Account;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.crm.FollowUpMapper;
import com.clinic.mapper.medical.MedicalRecordMapper;
import com.clinic.mapper.medical.ServiceOrderMapper;
import com.clinic.mapper.medical.ServiceResultMapper;
import com.clinic.mapper.prescription.PrescriptionMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceResultRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.prescription.PrescriptionRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.medical.MedicalRecordSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

import com.clinic.dto.medical.TriageRequest;
import com.clinic.entity.patient.PatientVitalProfile;
import com.clinic.repository.patient.PatientVitalProfileRepository;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final MedicalRecordMapper medicalRecordMapper;
    
    // Bổ sung các repository và mapper cho detail
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderMapper serviceOrderMapper;
    private final com.clinic.repository.medical.DoctorServicePriceRepository doctorServicePriceRepository;
    private final ServiceResultRepository serviceResultRepository;
    private final ServiceResultMapper serviceResultMapper;
    private final FollowUpRepository followUpRepository;
    private final FollowUpMapper followUpMapper;
    private final com.clinic.service.appointment.AppointmentService appointmentService;
    private final PatientVitalProfileRepository vitalProfileRepository;

    @Transactional
    public MedicalRecordResponse create(MedicalRecordRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));
        Staff mainDoctor = staffRepository.findById(request.getMainDoctorId())
                .orElseThrow(() -> new RuntimeException("Main doctor not found."));

        MedicalRecord record = medicalRecordMapper.toEntity(request);
        record.setPatient(patient);
        record.setMainDoctor(mainDoctor);

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public PageResponse<MedicalRecordResponse> getAll(MedicalRecordFilterRequest filter) {
        Specification<MedicalRecord> spec = MedicalRecordSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<MedicalRecord> page = medicalRecordRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(medicalRecordMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getAll() {
        return medicalRecordRepository.findAll().stream()
                .map(medicalRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalRecordResponse update(Integer recordId, MedicalRecordRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found."));

        record.setDiagnosis(request.getDiagnosis());
        record.setTreatment(request.getTreatment());
        record.setNote(request.getNote());
        
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
            
            if (request.getStatus() == com.clinic.common.enums.MedicalRecordStatus.DONE && record.getAppointment() != null) {
                // Tự động tính và lưu giá khám bác sĩ
                java.math.BigDecimal consultationFee = doctorServicePriceRepository.getBaseConsultationFeeByDoctorId(record.getMainDoctor().getStaffId());
                if (consultationFee == null) {
                    consultationFee = new java.math.BigDecimal("300000"); // Giá mặc định nếu chưa cấu hình
                }
                record.setConsultationFee(consultationFee);

                // Tự động tính tổng giá dịch vụ cận lâm sàng (Lab/Imaging)
                java.math.BigDecimal totalServiceFee = java.math.BigDecimal.ZERO;
                java.util.List<com.clinic.entity.medical.ServiceOrder> orders = serviceOrderRepository.findByMedicalRecordId(record.getRecordId());
                for (com.clinic.entity.medical.ServiceOrder order : orders) {
                    if (order.getStatus() != com.clinic.common.enums.ServiceOrderStatus.CANCELLED) {
                        java.math.BigDecimal price = order.getPriceAtTime();
                        if (price != null) {
                            totalServiceFee = totalServiceFee.add(price);
                        }
                    }
                }
                record.setServiceFee(totalServiceFee);

                appointmentService.updateStatus(record.getAppointment().getAppointmentId(), com.clinic.common.enums.AppointmentStatus.COMPLETED);
            }
        }

        if (request.getUpdatedByDoctorId() != null) {
            Staff updatedBy = staffRepository.findById(request.getUpdatedByDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor tracking ID not found."));
            record.setUpdatedByDoctor(updatedBy);
            record.setEditReason(request.getEditReason());
        }

        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    @Transactional
    public MedicalRecordResponse updateTriage(Integer recordId, TriageRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found."));

        Patient patient = record.getPatient();
        PatientVitalProfile vitalProfile = vitalProfileRepository.findById(patient.getPatientId())
                .orElse(new PatientVitalProfile(patient.getPatientId(), patient, null, null, null, null, null, null, null, null, null));

        vitalProfile.setHeight(request.getHeight());
        vitalProfile.setWeight(request.getWeight());
        vitalProfile.setBloodPressure(request.getBloodPressure());
        vitalProfile.setPulse(request.getPulse());
        vitalProfile.setBloodType(request.getBloodType());
        vitalProfile.setAllergies(request.getAllergies());
        vitalProfile.setMedicalHistory(request.getChronicDiseases());
        // Temperature is not in DB, so we omit or add it to DB later. For now omit.
        
        vitalProfileRepository.save(vitalProfile);

        record.setVitalsTaken(true);
        return medicalRecordMapper.toResponse(medicalRecordRepository.save(record));
    }

    public List<MedicalRecordResponse> getMyRecords() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Patient patient = patientRepository.findByAccount_AccountId(account.getAccountId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        List<MedicalRecord> records = medicalRecordRepository.findByPatient_PatientId(patient.getPatientId());
        return records.stream()
                .map(medicalRecordMapper::toResponse)
                .collect(Collectors.toList());
    }

    public MedicalRecordDetailResponse getRecordDetail(Integer recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            String email = auth.getName();
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            Patient patient = patientRepository.findByAccount_AccountId(account.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            if (!record.getPatient().getPatientId().equals(patient.getPatientId())) {
                throw new RuntimeException("Access denied");
            }
        }
        
        PrescriptionResponse prescriptionResponse = null;
        var prescriptionOpt = prescriptionRepository.findByMedicalRecordId(record.getRecordId());
        if (prescriptionOpt.isPresent()) {
            prescriptionResponse = prescriptionMapper.toResponse(prescriptionOpt.get());
        }
        
        List<ServiceOrderResponse> orderResponses = serviceOrderRepository.findByMedicalRecordId(record.getRecordId())
                .stream()
                .map(order -> {
                    ServiceOrderResponse resp = serviceOrderMapper.toResponse(order);
                    serviceResultRepository.findByOrderId(order.getOrderId())
                            .ifPresent(result -> resp.setResult(serviceResultMapper.toResponse(result)));
                    return resp;
                })
                .collect(Collectors.toList());
        
        List<FollowUpResponse> followUpResponses = followUpRepository.findByMedicalRecordId(record.getRecordId())
                .stream()
                .map(followUpMapper::toResponse)
                .collect(Collectors.toList());
        
        return MedicalRecordDetailResponse.builder()
                .recordId(record.getRecordId())
                .patientId(record.getPatient().getPatientId())
                .patientFullName(record.getPatient().getFullName())
                .patientGender(record.getPatient().getGender())
                .patientDob(record.getPatient().getDateOfBirth())
                .patientPhone(record.getPatient().getPhone())
                .patientAddress(record.getPatient().getAddress())
                .appointmentId(record.getAppointment() != null ? record.getAppointment().getAppointmentId() : null)
                .mainDoctorId(record.getMainDoctor().getStaffId())
                .mainDoctorName(record.getMainDoctor().getFullName())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .note(record.getNote())
                .status(record.getStatus() != null ? record.getStatus().name() : null)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .consultationFee(record.getConsultationFee())
                .serviceFee(record.getServiceFee())
                .prescription(prescriptionResponse)
                .serviceOrders(orderResponses)
                .followUps(followUpResponses)
                .build();
    }
}