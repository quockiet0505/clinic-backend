package com.clinic.service.staff;

import java.util.List;
import java.util.stream.Collectors;

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
    public StaffScheduleResponse create(StaffScheduleRequest request) {
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
    public StaffScheduleResponse update(Integer id, StaffScheduleRequest request) {
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
    public void delete(Integer id) {
        if (!scheduleRepository.existsById(id)) {
            throw new RuntimeException("Schedule not found");
        }
        scheduleRepository.deleteById(id); // Hard delete for schedules is fine
    }
}