package com.clinic.service.appointment;

import com.clinic.dto.appointment.TimeSlotResponse;
import com.clinic.entity.staff.Staff;
import com.clinic.common.enums.StaffType;
import com.clinic.common.enums.ServiceType;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.StaffScheduleRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.util.HolidayUtils;
import com.clinic.entity.staff.StaffSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {

    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(
            Integer doctorId, Integer expertiseId, Integer serviceId, LocalDate date) {
        if (doctorId != null) {
            return buildSlotsForDoctor(doctorId, date, null);
        }
        if (expertiseId != null) {
            return buildSlotsForExpertise(expertiseId, date);
        }
        if (serviceId != null) {
            com.clinic.entity.medical.Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            if (service.getServiceType().isHiddenEverywhere()) {
                throw new RuntimeException("Dịch vụ khám tổng quát (EXAM) hiện không hỗ trợ đặt lịch.");
            }
            if (!service.getServiceType().isPatientBookable()) {
                throw new RuntimeException(
                        "Dịch vụ này chỉ được chỉ định trong quá trình khám, không đặt lịch trực tiếp.");
            }
            Integer duration = service.getEstimatedDuration() != null ? service.getEstimatedDuration() : 15;
            return buildSlotsForLabStaff(date, duration);
        }
        return List.of();
    }

    private List<TimeSlotResponse> buildSlotsForExpertise(Integer expertiseId, LocalDate date) {
        List<Staff> doctors = expertiseId != null
                ? staffRepository.findByExpertise_ExpertiseIdAndStaffTypeAndIsDeleted(
                        expertiseId, StaffType.DOCTOR, 0)
                : staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);

        Map<String, TimeSlotResponse> merged = new LinkedHashMap<>();
        for (Staff doctor : doctors) {
            for (TimeSlotResponse slot : buildSlotsForDoctor(doctor.getStaffId(), date, doctor)) {
                if (!slot.isAvailable()) {
                    continue;
                }
                String key = slot.getTimeStart() + "-" + slot.getTimeEnd();
                merged.putIfAbsent(key, slot);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(TimeSlotResponse::getTimeStart))
                .collect(Collectors.toList());
    }

    private List<TimeSlotResponse> buildSlotsForLabStaff(LocalDate date, Integer intervalMinutes) {
        int slotMinutes = intervalMinutes != null && intervalMinutes > 0 ? intervalMinutes : 30;
        List<Staff> labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.LAB_TECH, 0);
        if (labStaff.isEmpty()) {
            labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.STAFF, 0);
        }
        Map<String, TimeSlotResponse> merged = new LinkedHashMap<>();
        for (Staff staff : labStaff) {
            for (TimeSlotResponse slot : buildSlotsForDoctor(staff.getStaffId(), date, staff, slotMinutes)) {
                if (!slot.isAvailable()) {
                    continue;
                }
                String key = slot.getTimeStart() + "-" + slot.getTimeEnd();
                merged.putIfAbsent(key, slot);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(TimeSlotResponse::getTimeStart))
                .collect(Collectors.toList());
    }

    private List<TimeSlotResponse> buildSlotsForDoctor(Integer doctorId, LocalDate date, Staff staffRef) {
        return buildSlotsForDoctor(doctorId, date, staffRef, 30);
    }

    private List<TimeSlotResponse> buildSlotsForDoctor(
            Integer doctorId, LocalDate date, Staff staffRef, int intervalMinutes) {
        List<TimeSlotResponse> slots = new ArrayList<>();
        int slotMinutes = intervalMinutes > 0 ? intervalMinutes : 30;

        List<StaffSchedule> schedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(doctorId, date);
        if (schedules.isEmpty()) {
            return slots;
        }

        if (leaveRequestRepository.isDoctorOnLeave(doctorId, date)) {
            return slots;
        }

        for (StaffSchedule schedule : schedules) {
            LocalTime current = schedule.getStartTime();
            while (current.isBefore(schedule.getEndTime())
                    && !current.plusMinutes(slotMinutes).isAfter(schedule.getEndTime())) {
                slots.add(createSlot(doctorId, date, current, staffRef, slotMinutes));
                current = current.plusMinutes(slotMinutes);
            }
        }

        return slots;
    }

    private TimeSlotResponse createSlot(Integer doctorId, LocalDate date, LocalTime start, Staff staffRef) {
        return createSlot(doctorId, date, start, staffRef, 30);
    }

    private TimeSlotResponse createSlot(
            Integer doctorId, LocalDate date, LocalTime start, Staff staffRef, int intervalMinutes) {
        int slotMinutes = intervalMinutes > 0 ? intervalMinutes : 30;
        boolean isBooked = appointmentRepository
                .existsByMainDoctor_StaffIdAndAppointmentDateAndTimeStartAndIsDeleted(
                        doctorId, date, start, 0);

        TimeSlotResponse slot = new TimeSlotResponse();
        slot.setTimeStart(start);
        slot.setTimeEnd(start.plusMinutes(slotMinutes));
        slot.setAvailable(!isBooked);
        
        if (staffRef != null) {
            slot.setDoctorId(staffRef.getStaffId());
            slot.setDoctorName(staffRef.getFullName());
        } else {
            staffRepository.findById(doctorId).ifPresent(doc -> {
                slot.setDoctorId(doc.getStaffId());
                slot.setDoctorName(doc.getFullName());
            });
        }
        
        return slot;
    }
}
