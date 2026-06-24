package com.clinic.service.appointment;

import com.clinic.dto.appointment.TimeSlotResponse;
import com.clinic.entity.staff.Staff;
import com.clinic.common.enums.StaffType;
import com.clinic.common.enums.ServiceType;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.util.HolidayUtils;
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
            if (service.getServiceType() == ServiceType.EXAM) {
                return buildSlotsForExpertise(null, date);
            }
            return buildSlotsForLabStaff(date);
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

    private List<TimeSlotResponse> buildSlotsForLabStaff(LocalDate date) {
        List<Staff> labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.LAB_TECH, 0);
        if (labStaff.isEmpty()) {
            labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.STAFF, 0);
        }
        Map<String, TimeSlotResponse> merged = new LinkedHashMap<>();
        for (Staff staff : labStaff) {
            for (TimeSlotResponse slot : buildSlotsForDoctor(staff.getStaffId(), date, staff)) {
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
        List<TimeSlotResponse> slots = new ArrayList<>();
        
        // 1. Check weekends
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return slots;
        }
        
        // 2. Check holidays
        if (HolidayUtils.isHoliday(date)) {
            return slots;
        }

        // 3. Check leaves
        if (leaveRequestRepository.isDoctorOnLeave(doctorId, date)) {
            return slots;
        }

        // 4. Generate slots
        LocalTime[] morningSlots = {
            LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0), LocalTime.of(9, 30),
            LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30)
        };
        LocalTime[] afternoonSlots = {
            LocalTime.of(13, 30), LocalTime.of(14, 0), LocalTime.of(14, 30), LocalTime.of(15, 0),
            LocalTime.of(15, 30), LocalTime.of(16, 0), LocalTime.of(16, 30), LocalTime.of(17, 0)
        };

        for (LocalTime start : morningSlots) {
            slots.add(createSlot(doctorId, date, start, staffRef));
        }
        for (LocalTime start : afternoonSlots) {
            slots.add(createSlot(doctorId, date, start, staffRef));
        }

        return slots;
    }

    private TimeSlotResponse createSlot(Integer doctorId, LocalDate date, LocalTime start, Staff staffRef) {
        boolean isBooked = appointmentRepository
                .existsByMainDoctor_StaffIdAndAppointmentDateAndTimeStartAndIsDeleted(
                        doctorId, date, start, 0);

        TimeSlotResponse slot = new TimeSlotResponse();
        slot.setTimeStart(start);
        slot.setTimeEnd(start.plusMinutes(30));
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
