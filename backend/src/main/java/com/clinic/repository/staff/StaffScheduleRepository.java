package com.clinic.repository.staff;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinic.entity.staff.StaffSchedule;

public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, Integer> {
    // Find all schedules for a specific doctor on a specific date
    List<StaffSchedule> findByStaff_StaffIdAndWorkingDate(Integer staffId, LocalDate workingDate);
}