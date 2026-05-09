package com.clinic.repository.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.appointment.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByIsDeleted(Integer isDeleted);
    
    // Crucial for checking exact time overlaps
    boolean existsByMainDoctor_StaffIdAndAppointmentDateAndTimeStartAndIsDeleted(
            Integer doctorId, LocalDate date, LocalTime startTime, Integer isDeleted);
}