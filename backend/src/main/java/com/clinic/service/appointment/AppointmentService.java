package com.clinic.service.appointment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.CancelledByType;
import com.clinic.common.enums.ScheduleStatus;
import com.clinic.common.enums.StaffType;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.StaffSchedule;
import com.clinic.mapper.appointment.AppointmentMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.ServiceRepository;
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
    private final ServiceRepository serviceRepository;
    private final StaffScheduleRepository scheduleRepository;
    private final AppointmentMapper appointmentMapper;

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        // email may be username; you need to fetch patient by account email
        // adapt to your actual account retrieval method
        // For simplicity, assume you have a method to get patient by email
        return patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    private Staff autoAssignDoctor(Integer expertiseId) {
        List<Staff> availableDoctors = staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);
        if (expertiseId != null) {
            availableDoctors = availableDoctors.stream()
                    .filter(d -> d.getExpertise() != null && d.getExpertise().getExpertiseId().equals(expertiseId))
                    .collect(Collectors.toList());
        }
        if (availableDoctors.isEmpty()) {
            throw new RuntimeException("No doctor available for the selected criteria");
        }
        return availableDoctors.get(0);
    }

    private void validateAppointmentLogic(AppointmentRequest request, Integer patientId, Integer doctorId) {
        // Skip validation if no doctor assigned
        if (doctorId == null) {
            // only check patient double booking
            boolean isPatientConflict = appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
                    patientId, request.getAppointmentDate(), request.getTimeStart(), 0);
            if (isPatientConflict) {
                throw new RuntimeException("You already have an appointment booked at this exact time.");
            }
            return;
        }

        // Spam check for online bookings
        if (request.getAppointmentType() == AppointmentType.ONLINE) {
            long spamCount = appointmentRepository.countByPatient_PatientIdAndStatusAndCancelReasonContainingAndIsDeleted(
                    patientId, AppointmentStatus.CANCELLED, "[SPAM]", 0);
            if (spamCount >= 3) {
                throw new RuntimeException("Account locked due to repeated late cancellations. Please book at clinic.");
            }
            LocalDateTime requestedDateTime = LocalDateTime.of(request.getAppointmentDate(), request.getTimeStart());
            if (requestedDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
                throw new RuntimeException("Online appointments must be booked at least 24 hours in advance.");
            }
        }

        // Patient double booking
        boolean isPatientConflict = appointmentRepository.existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
                patientId, request.getAppointmentDate(), request.getTimeStart(), 0);
        if (isPatientConflict) {
            throw new RuntimeException("You already have an appointment at this time.");
        }

        // Doctor availability
        List<StaffSchedule> dailySchedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(
                doctorId, request.getAppointmentDate());
        if (dailySchedules.isEmpty()) {
            throw new RuntimeException("Doctor is not scheduled on this date.");
        }

        // End of shift for walk-ins
        if (request.getAppointmentType() == AppointmentType.WALK_IN && request.getTimeStart() != null) {
            LocalTime latestEndTime = dailySchedules.stream()
                    .filter(s -> s.getStatus() != ScheduleStatus.OFF)
                    .map(StaffSchedule::getEndTime)
                    .max(LocalTime::compareTo)
                    .orElse(LocalTime.MAX);
            if (request.getTimeStart().plusMinutes(15).isAfter(latestEndTime)) {
                throw new RuntimeException("Doctor is ending shift soon. Cannot accept more walk-ins.");
            }
        }
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        Patient patient = getCurrentPatient();

        // Handle doctor assignment
        Staff doctor = null;
        if (request.getMainDoctorId() != null) {
            doctor = staffRepository.findById(request.getMainDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
        } else {
            doctor = autoAssignDoctor(request.getExpertiseId());
            request.setMainDoctorId(doctor.getStaffId());
        }

        // Validate
        validateAppointmentLogic(request, patient.getPatientId(), request.getMainDoctorId());

        // Optional service
        com.clinic.entity.medical.Service service = null;
        if (request.getServiceId() != null) {
            service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Service not found"));
        }

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setMainDoctor(doctor);
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setIsDeleted(0);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    public AppointmentResponse updateStatus(Integer id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (newStatus == AppointmentStatus.CHECKED_IN && appointment.getStatus() == AppointmentStatus.PENDING) {
            appointment.setCheckinTime(LocalDateTime.now());
        }

        if (
            newStatus == AppointmentStatus.COMPLETED
        ) {
            appointment.setCheckoutTime(
                    LocalDateTime.now()
            );
        }
        
        appointment.setStatus(newStatus);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelByPatient(Integer id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getTimeStart());
        
        if (LocalDateTime.now().isAfter(appointmentDateTime.minusHours(3))) {
            throw new RuntimeException("Chỉ được phép hủy lịch trước thời gian khám ít nhất 3 tiếng.");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(CancelledByType.PATIENT);
        appointment.setCancelReason(reason);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse transferDoctor(Integer appointmentId, Integer newDoctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Staff newDoctor = staffRepository.findById(newDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        appointment.setMainDoctor(newDoctor);
        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllActive() {
        return appointmentRepository.findByIsDeleted(0).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments() {
        Patient patient = getCurrentPatient();
        return appointmentRepository.findByPatient_PatientIdAndIsDeletedOrderByAppointmentDateDesc(
                        patient.getPatientId(), 0)
                .stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getDetail(Integer id) {
        Patient patient = getCurrentPatient();
        Appointment appointment = appointmentRepository.findByAppointmentIdAndIsDeleted(id, 0)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        // optional: check that the appointment belongs to the current patient
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Access denied");
        }
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<com.clinic.dto.appointment.TimeSlotResponse> getAvailableSlots(Integer doctorId, java.time.LocalDate date) {
        List<com.clinic.dto.appointment.TimeSlotResponse> slots = new java.util.ArrayList<>();
        List<StaffSchedule> schedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(doctorId, date);
        
        if (schedules.isEmpty()) {
            return slots;
        }

        List<Appointment> existingAppointments = appointmentRepository.findByMainDoctor_StaffIdAndAppointmentDateAndIsDeleted(doctorId, date, 0);

        for (StaffSchedule schedule : schedules) {
            if (schedule.getStatus() == ScheduleStatus.OFF) continue;

            LocalTime start = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();
            
            // Assume 30 min slots
            LocalTime current = start;
            while (current.plusMinutes(30).isBefore(end) || current.plusMinutes(30).equals(end)) {
                LocalTime slotStart = current;
                LocalTime slotEnd = current.plusMinutes(30);
                
                boolean isAvailable = existingAppointments.stream().noneMatch(a -> 
                    (a.getStatus() != AppointmentStatus.CANCELLED) && 
                    ((a.getTimeStart() != null && a.getTimeStart().equals(slotStart)) || 
                     (a.getTimeStart() != null && a.getTimeStart().isBefore(slotEnd) && a.getTimeEnd() != null && a.getTimeEnd().isAfter(slotStart)))
                );

                slots.add(new com.clinic.dto.appointment.TimeSlotResponse(slotStart, slotEnd, isAvailable));
                current = current.plusMinutes(30);
            }
        }
        return slots;
    }
}