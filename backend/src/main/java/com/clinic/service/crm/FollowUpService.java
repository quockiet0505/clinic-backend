package com.clinic.service.crm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.common.enums.CreatedByType;
import com.clinic.common.enums.FollowUpStatus;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.crm.FollowUpFilterRequest;
import com.clinic.dto.crm.FollowUpRequest;
import com.clinic.dto.crm.FollowUpResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.crm.FollowUp;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.mapper.crm.FollowUpMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.specification.crm.FollowUpSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private static final DateTimeFormatter DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FollowUpRepository followUpRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final FollowUpMapper followUpMapper;
    private final NotificationService notificationService;

    @Transactional
    public FollowUpResponse create(FollowUpRequest request) {
        MedicalRecord record = medicalRecordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RuntimeException("Medical Record not found."));
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));
        Staff doctor = staffRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found."));

        FollowUp followUp = followUpMapper.toEntity(request);
        followUp.setMedicalRecord(record);
        followUp.setPatient(patient);
        followUp.setDoctor(doctor);
        followUp.setStatus(FollowUpStatus.PENDING);

        FollowUp saved = followUpRepository.save(followUp);
        notifyPatientFollowUpCreated(saved);

        return followUpMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowUpResponse> getAll(FollowUpFilterRequest filter) {
        Specification<FollowUp> spec = FollowUpSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<FollowUp> page = followUpRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(followUpMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getAll() {
        return followUpRepository.findAll().stream()
                .map(followUpMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FollowUpResponse updateStatus(Integer id, FollowUpStatus newStatus) {
        return updateStatus(id, newStatus, null);
    }

    @Transactional
    public FollowUpResponse updateStatus(Integer id, FollowUpStatus newStatus, String cancelReason) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up schedule not found."));

        followUp.setStatus(newStatus);

        if (newStatus == FollowUpStatus.CONFIRMED && followUp.getConfirmedAt() == null) {
            followUp.setConfirmedAt(LocalDateTime.now());
        }
        if (newStatus == FollowUpStatus.CANCELLED && cancelReason != null && !cancelReason.isBlank()) {
            followUp.setCancelReason(cancelReason.trim());
        }
        if (cancelReason != null && !cancelReason.isBlank() && followUp.getNote() != null) {
            followUp.setNote(followUp.getNote() + " | Log: " + cancelReason.trim());
        } else if (cancelReason != null && !cancelReason.isBlank()) {
            followUp.setNote("Log: " + cancelReason.trim());
        }

        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Transactional
    public FollowUpResponse linkAppointment(Integer id, Integer appointmentId) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up schedule not found."));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        followUp.setAppointment(appointment);
        if (followUp.getStatus() == FollowUpStatus.PENDING) {
            followUp.setStatus(FollowUpStatus.CONFIRMED);
            followUp.setConfirmedAt(LocalDateTime.now());
        }

        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Transactional
    public FollowUpResponse markReminderSent(Integer id) {
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Follow-up schedule not found."));
        followUp.setReminderSentAt(LocalDateTime.now());
        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> getMyFollowUps() {
        Patient patient = getCurrentPatient();
        return followUpRepository.findByPatient_PatientIdOrderByScheduledDatetimeDesc(patient.getPatientId())
                .stream()
                .map(followUpMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FollowUpResponse confirmByPatient(Integer followUpId) {
        Patient patient = getCurrentPatient();
        FollowUp followUp = followUpRepository.findByFollowUpIdAndPatient_PatientId(followUpId, patient.getPatientId())
                .orElseThrow(() -> new RuntimeException("Follow-up not found."));
        if (followUp.getStatus() == FollowUpStatus.CANCELLED || followUp.getStatus() == FollowUpStatus.COMPLETED) {
            throw new RuntimeException("Không thể xác nhận lịch tái khám ở trạng thái hiện tại.");
        }
        followUp.setStatus(FollowUpStatus.CONFIRMED);
        followUp.setConfirmedAt(LocalDateTime.now());
        
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setMainDoctor(followUp.getDoctor());
        appointment.setAppointmentDate(followUp.getScheduledDatetime().toLocalDate());
        appointment.setTimeStart(followUp.getScheduledDatetime().toLocalTime());
        appointment.setTimeEnd(followUp.getScheduledDatetime().toLocalTime().plusMinutes(30));
        appointment.setAppointmentType(AppointmentType.ONLINE);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setCreatedBy(CreatedByType.PATIENT);
        appointment.setBookingMode(BookingMode.DOCTOR);
        appointment.setIsAiSuggested(false);
        appointment.setNote("Tái khám: " + (followUp.getNote() != null ? followUp.getNote() : ""));
        
        Appointment savedAppt = appointmentRepository.save(appointment);
        followUp.setAppointment(savedAppt);
        
        FollowUp saved = followUpRepository.save(followUp);

        notificationService.createAndSendNotification(
                patient.getAccount().getAccountId(),
                "Bạn đã xác nhận lịch tái khám ngày "
                        + saved.getScheduledDatetime().format(DISPLAY_DATETIME)
                        + ". Hệ thống đã tự động đặt lịch vào khung giờ này.",
                "SYSTEM"
        );
        return followUpMapper.toResponse(saved);
    }

    @Transactional
    public FollowUpResponse declineByPatient(Integer followUpId, String reason) {
        Patient patient = getCurrentPatient();
        FollowUp followUp = followUpRepository.findByFollowUpIdAndPatient_PatientId(followUpId, patient.getPatientId())
                .orElseThrow(() -> new RuntimeException("Follow-up not found."));
        followUp.setStatus(FollowUpStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            followUp.setCancelReason(reason.trim());
        }
        return followUpMapper.toResponse(followUpRepository.save(followUp));
    }

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return patientRepository.findByAccount_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Patient not found."));
    }

    private void notifyPatientFollowUpCreated(FollowUp followUp) {
        Patient patient = followUp.getPatient();
        if (patient.getAccount() == null) {
            return;
        }

        String content = String.format(
                "Bác sĩ %s hẹn bạn tái khám vào %s.%s Vui lòng xác nhận và đặt lịch trên ứng dụng.",
                followUp.getDoctor().getFullName(),
                followUp.getScheduledDatetime().format(DISPLAY_DATETIME),
                followUp.getNote() != null && !followUp.getNote().isBlank()
                        ? " Ghi chú: " + followUp.getNote() + "."
                        : ""
        );

        notificationService.createAndSendNotification(
                patient.getAccount().getAccountId(),
                content,
                "SYSTEM"
        );
    }
}
