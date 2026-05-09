package com.clinic.service.appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
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
    private final StaffScheduleRepository scheduleRepository; // Used for doctor availability validation
    private final AppointmentMapper appointmentMapper;

    // --- INTERNAL LOGIC & VALIDATION ---
    private void validateAppointmentLogic(AppointmentRequest request) {
        if (request.getTimeStart() != null && request.getTimeEnd() != null) {
            
            // 1. Basic time sanity check
            if (request.getTimeStart().isAfter(request.getTimeEnd())) {
                throw new RuntimeException("Start time must be before end time.");
            }

            // 2. Check if Doctor is scheduled to work on this day
            List<StaffSchedule> dailySchedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(
                    request.getMainDoctorId(), request.getAppointmentDate());
            
            if (dailySchedules.isEmpty()) {
                throw new RuntimeException("Doctor has no working schedule on this date.");
            }

            // 3. Check if the requested time falls within the doctor's working shifts
            boolean isWithinWorkingHours = false;
            for (StaffSchedule shift : dailySchedules) {
                if (shift.getStatus() == ScheduleStatus.OFF) continue; // Skip breaks/days off
                
                if (!request.getTimeStart().isBefore(shift.getStartTime()) && 
                    !request.getTimeEnd().isAfter(shift.getEndTime())) {
                    isWithinWorkingHours = true;
                    break;
                }
            }

            if (!isWithinWorkingHours) {
                throw new RuntimeException("The requested time is outside the doctor's working hours or the doctor is OFF.");
            }

            // 4. Check for double booking (Does doctor already have an appointment at this exact start time?)
            boolean isConflict = appointmentRepository.existsByMainDoctor_StaffIdAndAppointmentDateAndTimeStartAndIsDeleted(
                    request.getMainDoctorId(), request.getAppointmentDate(), request.getTimeStart(), 0);
            
            if (isConflict) {
                throw new RuntimeException("Doctor already has an active appointment booked at this exact time.");
            }
        }
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        // Run strict validations
        validateAppointmentLogic(request);

        // Fetch valid related entities
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found in system."));
        Staff doctor = staffRepository.findById(request.getMainDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found in system."));

        // Map and save
        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setMainDoctor(doctor);
        appointment.setStatus(AppointmentStatus.PENDING);

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllActive() {
        return appointmentRepository.findByIsDeleted(0).stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateStatus(Integer id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (appointment.getIsDeleted() == 1) {
            throw new RuntimeException("Cannot update a deleted appointment.");
        }

        appointment.setStatus(newStatus);
        
        // Auto-record physical timestamps based on status flow
        if (newStatus == AppointmentStatus.CHECKED_IN) {
            appointment.setCheckinTime(LocalDateTime.now());
        } else if (newStatus == AppointmentStatus.COMPLETED) {
            appointment.setCheckoutTime(LocalDateTime.now());
        }

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public void softDelete(Integer id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));
        
        appointment.setIsDeleted(1);
        appointment.setStatus(AppointmentStatus.CANCELLED); // Automatically cancel when deleted
        appointmentRepository.save(appointment);
    }
}