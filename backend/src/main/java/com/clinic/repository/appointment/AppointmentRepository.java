package com.clinic.repository.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.entity.appointment.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Find all appointments by deletion status
    List<Appointment> findByIsDeleted(Integer isDeleted);

    // Check for doctor double booking at a specific date and time
    boolean existsByMainDoctor_StaffIdAndAppointmentDateAndTimeStartAndIsDeleted(
            Integer doctorId, LocalDate date, LocalTime timeStart, Integer isDeleted);

    // Check for patient double booking (Cannot be in two places at once)
    boolean existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
            Integer patientId, LocalDate date, LocalTime timeStart, Integer isDeleted);

    // Get appointments for a specific date and status (Used for automated background jobs)
    List<Appointment> findByAppointmentDateAndStatusAndIsDeleted(
            LocalDate date, AppointmentStatus status, Integer isDeleted);

    // Count the number of times a patient has been marked as SPAM or NO_SHOW
    long countByPatient_PatientIdAndStatusAndCancelReasonContainingAndIsDeleted(
            Integer patientId, AppointmentStatus status, String cancelReasonKeyword, Integer isDeleted);

    // Get all appointments by patient, ordered by appointment date descending
    List<Appointment> findByPatient_PatientIdAndIsDeletedOrderByAppointmentDateDesc(
            Integer patientId, Integer isDeleted);

    // Optional: find by id and not deleted (for detail with security)
    Optional<Appointment> findByAppointmentIdAndIsDeleted(Integer id, Integer isDeleted);

    List<Appointment> findByMainDoctor_StaffIdAndAppointmentDateAndIsDeleted(Integer doctorId, LocalDate date, Integer isDeleted);
}