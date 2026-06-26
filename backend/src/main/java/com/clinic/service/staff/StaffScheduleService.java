package com.clinic.service.staff;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.clinic.common.enums.ScheduleStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dto.staff.StaffScheduleRequest;
import com.clinic.dto.staff.StaffScheduleResponse;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.StaffSchedule;
import com.clinic.mapper.staff.StaffScheduleMapper;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.StaffScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffScheduleService {

    private final StaffScheduleRepository scheduleRepository;
    private final StaffRepository staffRepository;
    private final StaffScheduleMapper scheduleMapper;

    @Transactional
    public StaffScheduleResponse create( StaffScheduleRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        StaffSchedule schedule = scheduleMapper.toEntity(request);
        schedule.setStaff(staff);

        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<StaffScheduleResponse> getAll() {
        return scheduleRepository.findAll().stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffScheduleResponse update(@NonNull Integer id , StaffScheduleRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        StaffSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setWorkingDate(request.getWorkingDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(request.getStatus());
        schedule.setNote(request.getNote());

        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Schedule not found");
        }
        scheduleRepository.deleteById(id); // Hard delete for schedules is fine
    }

    @Transactional
    public void autoGenerateSchedules(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            generateScheduleForDate(date);
        }
    }

    @Transactional
    public void generateScheduleForDate(LocalDate date) {
        // Skip weekends
        if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            return;
        }
        // Skip holidays
        if (com.clinic.util.HolidayUtils.isHoliday(date)) {
            return;
        }
        
        List<Staff> staffs = staffRepository.findAll();
        for (Staff staff : staffs) {
            if (staff.getIsDeleted() == 1) continue;
            
            List<StaffSchedule> existing = scheduleRepository.findByStaff_StaffIdAndWorkingDate(staff.getStaffId(), date);
            if (!existing.isEmpty()) continue;

            // Morning: 07:30 - 11:30
            StaffSchedule morning = new StaffSchedule();
            morning.setStaff(staff);
            morning.setWorkingDate(date);
            morning.setStartTime(LocalTime.of(7, 30));
            morning.setEndTime(LocalTime.of(11, 30));
            morning.setStatus(ScheduleStatus.WORKING);
            scheduleRepository.save(morning);

            // Afternoon: 13:30 - 17:00
            StaffSchedule afternoon = new StaffSchedule();
            afternoon.setStaff(staff);
            afternoon.setWorkingDate(date);
            afternoon.setStartTime(LocalTime.of(13, 30));
            afternoon.setEndTime(LocalTime.of(17, 0));
            afternoon.setStatus(ScheduleStatus.WORKING);
            scheduleRepository.save(afternoon);
        }
    }

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    @Transactional
    public void maintainRollingSchedule() {
        LocalDate today = LocalDate.now();
        // Always ensure we have schedules for the next 30 days
        for (int i = 0; i <= 30; i++) {
            generateScheduleForDate(today.plusDays(i));
        }
    }
}