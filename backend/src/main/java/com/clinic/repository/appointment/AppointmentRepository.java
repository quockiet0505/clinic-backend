package com.clinic.repository.appointment;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.entity.appointment.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByIsDeleted(Integer isDeleted);

    boolean existsByMainDoctor_StaffIdAndAppointmentDateAndTimeStartAndIsDeleted(
            Integer doctorId, LocalDate date, LocalTime timeStart, Integer isDeleted);

    boolean existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
            Integer patientId, LocalDate date, LocalTime timeStart, Integer isDeleted);

    List<Appointment> findByAppointmentDateAndStatusAndIsDeleted(
            LocalDate date, AppointmentStatus status, Integer isDeleted);

    long countByPatient_PatientIdAndStatusAndCancelReasonContainingAndIsDeleted(
            Integer patientId, AppointmentStatus status, String cancelReasonKeyword, Integer isDeleted);

    List<Appointment> findByPatient_PatientIdAndIsDeletedOrderByAppointmentDateDesc(
            Integer patientId, Integer isDeleted);

    Optional<Appointment> findByAppointmentIdAndIsDeleted(Integer id, Integer isDeleted);

    List<Appointment> findByMainDoctor_StaffIdAndAppointmentDateAndIsDeleted(
            Integer doctorId, LocalDate date, Integer isDeleted);

    
    List<Appointment> findByAppointmentDateBetweenAndIsDeleted(
            LocalDate startDate, LocalDate endDate, Integer isDeleted);

    List<Appointment> findByMainDoctor_StaffIdAndAppointmentDateBetweenAndIsDeleted(
            Integer doctorId, LocalDate startDate, LocalDate endDate, Integer isDeleted);

    @Query("SELECT MAX(a.queueNumber) FROM Appointment a WHERE a.mainDoctor.staffId = :doctorId AND a.appointmentDate = :date AND a.isDeleted = 0")
    Optional<Integer> findMaxQueueNumberByDoctorAndDate(@Param("doctorId") Integer doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.patientId = :patientId AND a.appointmentDate BETWEEN :start AND :end AND a.isDeleted = 0")
    long countByPatientIdAndAppointmentDateBetween(
            @Param("patientId") Integer patientId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(s.originalPrice), 0) FROM Appointment a JOIN a.service s WHERE a.patient.patientId = :patientId AND a.appointmentDate BETWEEN :start AND :end AND a.status = 'COMPLETED' AND a.isDeleted = 0")
    double sumServicePriceByPatientIdAndAppointmentDateBetween(
            @Param("patientId") Integer patientId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.mainDoctor.staffId = :staffId AND a.status = 'COMPLETED' AND a.isDeleted = 0")
    Integer countCompletedAppointmentsByDoctorId(@Param("staffId") Integer staffId);
}