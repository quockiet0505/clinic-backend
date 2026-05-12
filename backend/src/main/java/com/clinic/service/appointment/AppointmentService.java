package com.clinic.service.appointment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.CancelledByType;
import com.clinic.common.enums.ScheduleStatus;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.StaffSchedule;
import com.clinic.mapper.appointment.AppointmentMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.StaffScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final StaffScheduleRepository scheduleRepository;
    private final AppointmentMapper appointmentMapper;

    // --- INTERNAL VALIDATION LOGIC ---
    private void validateAppointmentLogic(AppointmentRequest request) {
        
        // 1. Check Spam limitations for Online bookings
        if (request.getAppointmentType() == AppointmentType.ONLINE) {
            long spamCount = appointmentRepository.countByPatient_PatientIdAndStatusAndCancelReasonContainingAndIsDeleted(
                    request.getPatientId(), AppointmentStatus.CANCELLED, "[SPAM]", 0);
            
            if (spamCount >= 3) {
                throw new RuntimeException("Account is locked for online booking due to 3 instances of NO-SHOW or late cancellation. Please book directly at the clinic.");
            }

            // 2. Time constraint: Online appointments must be booked at least 24 hours in advance
            LocalDateTime requestedDateTime = LocalDateTime.of(request.getAppointmentDate(), request.getTimeStart());
            if (requestedDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
                throw new RuntimeException("Online appointments must be booked at least 24 hours in advance.");
            }
        }

        // 3. Check patient schedule conflict (Prevent double booking)
        boolean isPatientConflict = appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
                request.getPatientId(), request.getAppointmentDate(), request.getTimeStart(), 0);
        if (isPatientConflict) {
            throw new RuntimeException("You already have an appointment booked at this exact time.");
        }

        // 4. Check doctor's availability and working hours
        List<StaffSchedule> dailySchedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(
                request.getMainDoctorId(), request.getAppointmentDate());
        
        if (dailySchedules.isEmpty()) {
            throw new RuntimeException("Doctor is not scheduled to work on this date.");
        }

        // 5. End of shift constraint (Applies to Walk-ins arriving near closing time)
        if (request.getAppointmentType() == AppointmentType.WALK_IN && request.getTimeStart() != null) {
            LocalTime latestEndTime = dailySchedules.stream()
                .filter(s -> s.getStatus() != ScheduleStatus.OFF)
                .map(StaffSchedule::getEndTime)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.MAX);
            
            // Assuming an average examination takes 15 minutes
            if (request.getTimeStart().plusMinutes(15).isAfter(latestEndTime)) { 
                throw new RuntimeException("Doctor is ending shift soon. Cannot accept more walk-ins.");
            }
        }
    }

    // --- MAIN OPERATIONS ---

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        validateAppointmentLogic(request);

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found."));
        Staff doctor = staffRepository.findById(request.getMainDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found."));

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setMainDoctor(doctor);
        
        // Walk-ins are checked-in immediately, Online bookings are pending
        if (request.getAppointmentType() == AppointmentType.WALK_IN) {
            appointment.setStatus(AppointmentStatus.CHECKED_IN);
            appointment.setCheckinTime(LocalDateTime.now());
            // TODO: Generate Queue Number logic here (if applicable)
        } else {
            appointment.setStatus(AppointmentStatus.PENDING);
        }
        
        appointment.setIsDeleted(0);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse updateStatus(Integer id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        // Set actual check-in time when an online patient arrives at the counter
        if (newStatus == AppointmentStatus.CHECKED_IN && appointment.getStatus() == AppointmentStatus.PENDING) {
            appointment.setCheckinTime(LocalDateTime.now());
        }

        appointment.setStatus(newStatus);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    /**
     * Patient self-cancels the online appointment.
     * Rule: Cancellation < 3 hours before start time is marked as SPAM.
     */
    @Transactional
    public AppointmentResponse cancelByPatient(Integer id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));
        
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getTimeStart());
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(CancelledByType.PATIENT);

        if (LocalDateTime.now().isAfter(appointmentDateTime.minusHours(3))) {
            appointment.setCancelReason(reason + " [SPAM: Cancelled less than 3 hours before start time]");
        } else {
            appointment.setCancelReason(reason);
        }
        
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    /**
     * Receptionist reschedules or transfers the patient to another doctor.
     * Use case: Wrong department booked or doctor is unexpectedly unavailable.
     */
    @Transactional
    public AppointmentResponse transferDoctor(Integer appointmentId, Integer newDoctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));
        Staff newDoctor = staffRepository.findById(newDoctorId)
                .orElseThrow(() -> new RuntimeException("New Doctor not found."));

        appointment.setMainDoctor(newDoctor);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllActive() {
        return appointmentRepository.findByIsDeleted(0).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }
}