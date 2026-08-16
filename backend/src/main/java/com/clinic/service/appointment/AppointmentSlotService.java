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
            Integer doctorId, Integer expertiseId, Integer serviceId, LocalDate date, Integer ignoreAppointmentId) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        if (doctorId != null) {
            return buildSlotsForDoctor(doctorId, date, null, 30, ignoreAppointmentId);
        }
        if (expertiseId != null) {
            return buildSlotsForExpertise(expertiseId, date, ignoreAppointmentId);
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
            int duration = 30;
            return buildSlotsForLabStaff(date, duration, ignoreAppointmentId);
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(
            Integer doctorId, Integer expertiseId, Integer serviceId, LocalDate date) {
        return getAvailableSlots(doctorId, expertiseId, serviceId, date, null);
    }

    private List<TimeSlotResponse> buildSlotsForExpertise(Integer expertiseId, LocalDate date, Integer ignoreAppointmentId) {
        List<Staff> doctors = expertiseId != null
                ? staffRepository.findByExpertise_ExpertiseIdAndStaffTypeAndIsDeleted(
                        expertiseId, StaffType.DOCTOR, 0)
                : staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);

        Map<String, TimeSlotResponse> merged = new LinkedHashMap<>();
        for (Staff doctor : doctors) {
            for (TimeSlotResponse slot : buildSlotsForDoctor(doctor.getStaffId(), date, doctor, 30, ignoreAppointmentId)) {
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

    private int countAvailableKtvAtTime(List<Staff> labStaff, LocalDate date, LocalTime start, int slotMinutes) {
        int count = 0;
        for (Staff staff : labStaff) {
            if (leaveRequestRepository.isDoctorOnLeave(staff.getStaffId(), date)) continue;
            List<StaffSchedule> schedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(staff.getStaffId(), date);
            for (StaffSchedule schedule : schedules) {
                if (!start.isBefore(schedule.getStartTime()) && !start.plusMinutes(slotMinutes).isAfter(schedule.getEndTime())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private List<TimeSlotResponse> buildSlotsForLabStaff(LocalDate date, Integer intervalMinutes, Integer ignoreAppointmentId) {
        int slotMinutes = intervalMinutes != null && intervalMinutes > 0 ? intervalMinutes : 30;
        List<Staff> labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.LAB_TECH, 0);
        if (labStaff.isEmpty()) {
            labStaff = staffRepository.findByStaffTypeAndIsDeleted(StaffType.NURSE, 0);
        }
        
        if (labStaff.isEmpty()) return List.of();

        Set<LocalTime> availableStartTimes = new TreeSet<>();
        for (Staff staff : labStaff) {
            List<StaffSchedule> schedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(staff.getStaffId(), date);
            if (leaveRequestRepository.isDoctorOnLeave(staff.getStaffId(), date)) continue;

            for (StaffSchedule schedule : schedules) {
                LocalTime current = schedule.getStartTime();
                while (current.isBefore(schedule.getEndTime())
                        && !current.plusMinutes(slotMinutes).isAfter(schedule.getEndTime())) {
                    availableStartTimes.add(current);
                    current = current.plusMinutes(slotMinutes);
                }
            }
        }

        List<TimeSlotResponse> finalSlots = new ArrayList<>();
        for (LocalTime start : availableStartTimes) {
            int activeKtvCount = countAvailableKtvAtTime(labStaff, date, start, slotMinutes);
            if (activeKtvCount == 0) continue;
            
            int maxOnlineCapacity = Math.max(1, (int) (activeKtvCount * 0.5));
            long bookedCount = appointmentRepository.countByBookingModeAndDateAndTimeStart(
                    com.clinic.common.enums.BookingMode.SERVICE, date, start);
            
            if (ignoreAppointmentId != null) {
                Optional<com.clinic.entity.appointment.Appointment> ignoredApp = appointmentRepository.findById(ignoreAppointmentId);
                if (ignoredApp.isPresent() && ignoredApp.get().getBookingMode() == com.clinic.common.enums.BookingMode.SERVICE 
                    && ignoredApp.get().getAppointmentDate().equals(date) && ignoredApp.get().getTimeStart().equals(start)) {
                    bookedCount--;
                }
            }
            
            TimeSlotResponse slot = new TimeSlotResponse();
            slot.setTimeStart(start);
            slot.setTimeEnd(start.plusMinutes(slotMinutes));
            slot.setAvailable(bookedCount < maxOnlineCapacity);
            finalSlots.add(slot);
        }

        return finalSlots;
    }

    private List<TimeSlotResponse> buildSlotsForDoctor(Integer doctorId, LocalDate date, Staff staffRef, Integer ignoreAppointmentId) {
        return buildSlotsForDoctor(doctorId, date, staffRef, 30, ignoreAppointmentId);
    }

    private List<TimeSlotResponse> buildSlotsForDoctor(
            Integer doctorId, LocalDate date, Staff staffRef, int intervalMinutes, Integer ignoreAppointmentId) {
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
                slots.add(createSlot(doctorId, date, current, staffRef, slotMinutes, ignoreAppointmentId));
                current = current.plusMinutes(slotMinutes);
            }
        }

        return slots;
    }

    private TimeSlotResponse createSlot(Integer doctorId, LocalDate date, LocalTime start, Staff staffRef, Integer ignoreAppointmentId) {
        return createSlot(doctorId, date, start, staffRef, 30, ignoreAppointmentId);
    }

    private TimeSlotResponse createSlot(
            Integer doctorId, LocalDate date, LocalTime start, Staff staffRef, int intervalMinutes, Integer ignoreAppointmentId) {
        int slotMinutes = intervalMinutes > 0 ? intervalMinutes : 30;
        
        boolean isBooked = false;
        List<com.clinic.entity.appointment.Appointment> bookings = appointmentRepository.findByMainDoctor_StaffIdAndAppointmentDateAndIsDeleted(doctorId, date, 0);
        for (com.clinic.entity.appointment.Appointment app : bookings) {
            if (app.getTimeStart().equals(start)) {
                if (ignoreAppointmentId == null || !app.getAppointmentId().equals(ignoreAppointmentId)) {
                    isBooked = true;
                    break;
                }
            }
        }

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
