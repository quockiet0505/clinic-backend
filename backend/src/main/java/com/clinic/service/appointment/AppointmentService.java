// src/main/java/com/clinic/service/appointment/AppointmentService.java
package com.clinic.service.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.common.enums.CancelledByType;
import com.clinic.common.enums.ScheduleStatus;
import com.clinic.common.enums.ServiceType;
import com.clinic.common.enums.StaffType;
import com.clinic.dto.appointment.AppointmentFilterRequest;
import com.clinic.dto.appointment.AppointmentRequest;
import com.clinic.dto.appointment.AppointmentResponse;
import com.clinic.dto.appointment.TimeSlotResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.entity.staff.StaffSchedule;
import com.clinic.mapper.appointment.AppointmentMapper;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;
import com.clinic.repository.staff.StaffScheduleRepository;
import com.clinic.repository.staff.LeaveRequestRepository;
import com.clinic.specification.appointment.AppointmentSpecification;
import com.clinic.util.FilterUtils;
import com.clinic.util.HolidayUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final ExpertiseRepository expertiseRepository;
    private final StaffScheduleRepository scheduleRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AppointmentMapper appointmentMapper;
    private final com.clinic.service.crm.NotificationService notificationService;

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getAll(AppointmentFilterRequest filter) {
        Specification<Appointment> spec = AppointmentSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Appointment> page = appointmentRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(this::enrichResponse));
    }

    private AppointmentResponse enrichResponse(Appointment appointment) {
        AppointmentResponse response = appointmentMapper.toResponse(appointment);
        if (appointment.getMainDoctor() != null && appointment.getAppointmentDate() != null) {
            boolean isBusy = leaveRequestRepository.isDoctorOnLeave(appointment.getMainDoctor().getStaffId(), appointment.getAppointmentDate());
            response.setIsDoctorBusy(isBusy);
        }
        return response;
    }

    private Patient getCurrentPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return patientRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    private BookingMode resolveBookingMode(AppointmentRequest request) {
        if (request.getBookingMode() != null) {
            return request.getBookingMode();
        }
        if (request.getMainDoctorId() != null) {
            return BookingMode.DOCTOR;
        }
        if (request.getServiceId() != null) {
            return BookingMode.SERVICE;
        }
        if (request.getExpertiseId() != null) {
            return BookingMode.EXPERTISE;
        }
        return BookingMode.DIRECT;
    }

    private Expertise loadExpertise(Integer expertiseId) {
        if (expertiseId == null) {
            return null;
        }
        return expertiseRepository.findById(expertiseId)
                .orElseThrow(() -> new RuntimeException("Expertise not found"));
    }

    private Staff loadDoctor(Integer doctorId) {
        return staffRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    private Staff autoAssignDoctor(Integer expertiseId) {
        List<Staff> doctors;
        if (expertiseId != null) {
            doctors = staffRepository.findByExpertise_ExpertiseIdAndStaffTypeAndIsDeleted(
                    expertiseId, StaffType.DOCTOR, 0);
        } else {
            doctors = staffRepository.findByStaffTypeAndIsDeleted(StaffType.DOCTOR, 0);
        }
        if (doctors.isEmpty()) {
            throw new RuntimeException("No doctor available for the selected criteria");
        }
        return doctors.get(0);
    }

    private boolean isSlotTakenByDoctor(Integer doctorId, LocalDate date, LocalTime slotStart, LocalTime slotEnd) {
        List<Appointment> existing = appointmentRepository
                .findByMainDoctor_StaffIdAndAppointmentDateAndIsDeleted(doctorId, date, 0);
        return existing.stream().anyMatch(a ->
                a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getStatus() != AppointmentStatus.NO_SHOW
                        && a.getTimeStart() != null
                        && (a.getTimeStart().equals(slotStart)
                                || (a.getTimeEnd() != null
                                        && a.getTimeStart().isBefore(slotEnd)
                                        && a.getTimeEnd().isAfter(slotStart))));
    }

    private void validateAppointmentLogic(AppointmentRequest request, Integer patientId, Integer doctorId,
            BookingMode mode) {
        boolean isPatientConflict = appointmentRepository
                .existsByPatient_PatientIdAndAppointmentDateAndTimeStartAndIsDeleted(
                        patientId, request.getAppointmentDate(), request.getTimeStart(), 0);
        if (isPatientConflict) {
            throw new RuntimeException("You already have an appointment at this time.");
        }

        if (request.getAppointmentType() == AppointmentType.ONLINE) {
            long spamCount = appointmentRepository
                    .countByPatient_PatientIdAndStatusAndCancelReasonContainingAndIsDeleted(
                            patientId, AppointmentStatus.CANCELLED, "[SPAM]", 0);
            if (spamCount >= 3) {
                throw new RuntimeException(
                        "Account locked due to repeated late cancellations. Please book at clinic.");
            }
            LocalDateTime requestedDateTime = LocalDateTime.of(
                    request.getAppointmentDate(), request.getTimeStart());
            if (requestedDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
                throw new RuntimeException("Online appointments must be booked at least 24 hours in advance.");
            }
        }

        if (doctorId == null) {
            return;
        }

        LocalDate date = request.getAppointmentDate();

        // 1. Weekend check
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Cannot book on weekends.");
        }

        // 2. Holiday check
        if (HolidayUtils.isHoliday(date)) {
            throw new RuntimeException("Cannot book on holidays.");
        }

        // 3. Leave check
        if (leaveRequestRepository.isDoctorOnLeave(doctorId, date)) {
            throw new RuntimeException("Doctor is on leave on this date.");
        }

        List<StaffSchedule> dailySchedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(
                doctorId, date);

        // 4. Default schedule check
        if (dailySchedules.isEmpty()) {
            LocalTime start = request.getTimeStart();
            if (start != null) {
                boolean isMorning = (!start.isBefore(LocalTime.of(7, 30)) && start.isBefore(LocalTime.of(11, 30)));
                boolean isAfternoon = (!start.isBefore(LocalTime.of(13, 30)) && start.isBefore(LocalTime.of(17, 0)));
                if (!isMorning && !isAfternoon) {
                    throw new RuntimeException("Requested time is outside default working hours.");
                }
            }
        }

        if (isSlotTakenByDoctor(doctorId, request.getAppointmentDate(),
                request.getTimeStart(), request.getTimeEnd())) {
            throw new RuntimeException("Selected time slot is no longer available.");
        }

        if (request.getAppointmentType() == AppointmentType.WALK_IN && request.getTimeStart() != null) {
            LocalTime latestEndTime;
            if (dailySchedules.isEmpty()) {
                latestEndTime = LocalTime.of(17, 0);
            } else {
                latestEndTime = dailySchedules.stream()
                        .filter(s -> s.getStatus() != ScheduleStatus.OFF)
                        .map(StaffSchedule::getEndTime)
                        .max(LocalTime::compareTo)
                        .orElse(LocalTime.MAX);
            }
            if (request.getTimeStart().plusMinutes(15).isAfter(latestEndTime)) {
                throw new RuntimeException("Doctor is ending shift soon. Cannot accept more walk-ins.");
            }
        }
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        Patient patient = null;
        if (request.getPatientId() != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isStaffOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));
            if (isStaffOrAdmin) {
                patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            } else {
                patient = getCurrentPatient();
            }
        } else {
            patient = getCurrentPatient();
        }

        BookingMode mode = resolveBookingMode(request);

        Expertise expertise = loadExpertise(request.getExpertiseId());
        Expertise suggestedExpertise = loadExpertise(request.getSuggestedExpertiseId());
        Staff doctor = null;
        com.clinic.entity.medical.Service service = null;

        switch (mode) {
            case DOCTOR -> {
                if (request.getMainDoctorId() == null) {
                    throw new RuntimeException("Doctor is required for DOCTOR booking mode.");
                }
                doctor = loadDoctor(request.getMainDoctorId());
                if (expertise == null && doctor.getExpertise() != null) {
                    expertise = doctor.getExpertise();
                }
            }
            case EXPERTISE -> {
                if (expertise == null) {
                    throw new RuntimeException("Expertise is required for EXPERTISE booking mode.");
                }
                if (request.getMainDoctorId() != null) {
                    doctor = loadDoctor(request.getMainDoctorId());
                } else {
                    doctor = autoAssignDoctor(expertise.getExpertiseId());
                    request.setMainDoctorId(doctor.getStaffId());
                }
            }
            case SERVICE -> {
                if (request.getServiceId() == null) {
                    throw new RuntimeException("Service is required for SERVICE booking mode.");
                }
                service = serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() -> new RuntimeException("Service not found"));
                if (service.getServiceType() == ServiceType.EXAM) {
                    if (request.getMainDoctorId() != null) {
                        doctor = loadDoctor(request.getMainDoctorId());
                    } else {
                        doctor = autoAssignDoctor(request.getExpertiseId());
                        request.setMainDoctorId(doctor.getStaffId());
                    }
                    if (expertise == null && doctor.getExpertise() != null) {
                        expertise = doctor.getExpertise();
                    }
                }
            }
            case DIRECT -> {
                if (request.getMainDoctorId() != null) {
                    doctor = loadDoctor(request.getMainDoctorId());
                    if (expertise == null && doctor.getExpertise() != null) {
                        expertise = doctor.getExpertise();
                    }
                } else if (expertise != null) {
                    doctor = autoAssignDoctor(expertise.getExpertiseId());
                    request.setMainDoctorId(doctor.getStaffId());
                }
            }
            default -> throw new RuntimeException("Unsupported booking mode.");
        }

        validateAppointmentLogic(request, patient.getPatientId(), request.getMainDoctorId(), mode);

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setPatient(patient);
        appointment.setMainDoctor(doctor);
        appointment.setService(service);
        appointment.setExpertise(expertise);
        appointment.setSuggestedExpertise(suggestedExpertise);
        appointment.setBookingMode(mode);
        appointment.setIsAiSuggested(Boolean.TRUE.equals(request.getIsAiSuggested()));
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setIsDeleted(0);

        if (request.getAppointmentType() == AppointmentType.WALK_IN) {
            appointment.setStatus(AppointmentStatus.CHECKED_IN);
            appointment.setCheckinTime(LocalDateTime.now());
            
            // Check if this is a priority walk-in (e.g. Emergency, VIP)
            if (Boolean.TRUE.equals(request.getIsPriority())) {
                appointment.setQueueNumber(0);
            } else {
                Integer maxQueue = appointmentRepository.findMaxQueueNumberByDoctorAndDate(
                        doctor != null ? doctor.getStaffId() : null, 
                        request.getAppointmentDate()).orElse(0);
                appointment.setQueueNumber(maxQueue + 1);
            }
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Send Notification
        String dateStr = request.getAppointmentDate().toString();
        String timeStr = request.getTimeStart().toString();
        if (request.getAppointmentType() == AppointmentType.WALK_IN) {
            notificationService.createAndSendNotification(
                    patient.getAccount().getAccountId(),
                    "Lịch khám trực tiếp của bạn đã được Check-in. Số thứ tự của bạn là " + savedAppointment.getQueueNumber() + ".",
                    "SYSTEM");
        } else {
            notificationService.createAndSendNotification(
                    patient.getAccount().getAccountId(),
                    "Lịch hẹn khám của bạn vào ngày " + dateStr + " lúc " + timeStr + " đã được tạo thành công.",
                    "SYSTEM");
        }

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse updateStatus(Integer id, AppointmentStatus newStatus, Boolean isPriority) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        boolean justCheckedIn = false;
        boolean justCompleted = false;

        if (newStatus == AppointmentStatus.CHECKED_IN && 
            (appointment.getStatus() == AppointmentStatus.PENDING || appointment.getStatus() == AppointmentStatus.CONFIRMED)) {
            appointment.setCheckinTime(LocalDateTime.now());
            
            if (Boolean.TRUE.equals(isPriority)) {
                appointment.setQueueNumber(0);
            } else {
                Integer maxQueue = appointmentRepository.findMaxQueueNumberByDoctorAndDate(
                        appointment.getMainDoctor() != null ? appointment.getMainDoctor().getStaffId() : null, 
                        appointment.getAppointmentDate()).orElse(0);
                appointment.setQueueNumber(maxQueue + 1);
            }
            
            justCheckedIn = true;
        }
        if (newStatus == AppointmentStatus.COMPLETED && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            appointment.setCheckoutTime(LocalDateTime.now());
            justCompleted = true;
        }
        appointment.setStatus(newStatus);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if (justCheckedIn) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Bạn đã check-in thành công. Vui lòng chờ đến lượt khám. Số thứ tự của bạn là " + savedAppointment.getQueueNumber() + ".",
                    "SYSTEM");
        } else if (justCompleted) {
            notificationService.createAndSendNotification(
                    appointment.getPatient().getAccount().getAccountId(),
                    "Ca khám của bạn đã hoàn tất. Cảm ơn bạn đã sử dụng dịch vụ của phòng khám.",
                    "SYSTEM");
        }

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse cancelByPatient(Integer id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), appointment.getTimeStart());
        if (LocalDateTime.now().isAfter(appointmentDateTime.minusHours(3))) {
            throw new RuntimeException("Chỉ được phép hủy lịch trước thời gian khám ít nhất 3 tiếng.");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledBy(CancelledByType.PATIENT);
        appointment.setCancelReason(reason);
        appointment.setIsDeleted(1);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Lịch hẹn khám ngày " + appointment.getAppointmentDate() + " lúc " + appointment.getTimeStart() + " đã bị hủy với lý do: " + reason,
                "SYSTEM");

        return enrichResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse transferDoctor(Integer appointmentId, Integer newDoctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Staff newDoctor = staffRepository.findById(newDoctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        appointment.setMainDoctor(newDoctor);
        if (appointment.getExpertise() == null && newDoctor.getExpertise() != null) {
            appointment.setExpertise(newDoctor.getExpertise());
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.createAndSendNotification(
                appointment.getPatient().getAccount().getAccountId(),
                "Lịch hẹn của bạn đã được chuyển sang Bác sĩ " + newDoctor.getFullName() + " phụ trách.",
                "SYSTEM");

        return enrichResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllActive() {
        return appointmentRepository.findByIsDeleted(0).stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments() {
        Patient patient = getCurrentPatient();
        return appointmentRepository.findByPatient_PatientIdAndIsDeletedOrderByAppointmentDateDesc(
                        patient.getPatientId(), 0)
                .stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getDetail(Integer id) {
        Patient patient = getCurrentPatient();
        Appointment appointment = appointmentRepository.findByAppointmentIdAndIsDeleted(id, 0)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            throw new RuntimeException("Access denied");
        }
        return enrichResponse(appointment);
    }

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

        List<StaffSchedule> schedules = scheduleRepository.findByStaff_StaffIdAndWorkingDate(doctorId, date);
        Staff doctor = staffRef != null ? staffRef : staffRepository.findById(doctorId).orElse(null);

        // 4. Default schedules if not specifically set
        if (schedules.isEmpty()) {
            return generateDefaultSlots(doctorId, date, doctor);
        }

        for (StaffSchedule schedule : schedules) {
            if (schedule.getStatus() == ScheduleStatus.OFF) {
                continue;
            }
            LocalTime current = schedule.getStartTime();
            LocalTime end = schedule.getEndTime();
            while (current.plusMinutes(30).isBefore(end) || current.plusMinutes(30).equals(end)) {
                LocalTime slotStart = current;
                LocalTime slotEnd = current.plusMinutes(30);
                boolean available = !isSlotTakenByDoctor(doctorId, date, slotStart, slotEnd);
                slots.add(TimeSlotResponse.builder()
                        .timeStart(slotStart)
                        .timeEnd(slotEnd)
                        .isAvailable(available)
                        .doctorId(doctor != null ? doctor.getStaffId() : doctorId)
                        .doctorName(doctor != null ? doctor.getFullName() : null)
                        .build());
                current = current.plusMinutes(30);
            }
        }
        return slots;
    }

    private List<TimeSlotResponse> generateDefaultSlots(Integer doctorId, LocalDate date, Staff doctor) {
        List<TimeSlotResponse> slots = new ArrayList<>();
        generateSlotsForRange(doctorId, date, doctor, LocalTime.of(7, 30), LocalTime.of(11, 30), slots);
        generateSlotsForRange(doctorId, date, doctor, LocalTime.of(13, 30), LocalTime.of(17, 0), slots);
        return slots;
    }

    private void generateSlotsForRange(Integer doctorId, LocalDate date, Staff doctor, LocalTime start, LocalTime end, List<TimeSlotResponse> slots) {
        LocalTime current = start;
        while (current.plusMinutes(30).isBefore(end) || current.plusMinutes(30).equals(end)) {
            LocalTime slotStart = current;
            LocalTime slotEnd = current.plusMinutes(30);
            boolean available = !isSlotTakenByDoctor(doctorId, date, slotStart, slotEnd);
            slots.add(TimeSlotResponse.builder()
                    .timeStart(slotStart)
                    .timeEnd(slotEnd)
                    .isAvailable(available)
                    .doctorId(doctor != null ? doctor.getStaffId() : doctorId)
                    .doctorName(doctor != null ? doctor.getFullName() : null)
                    .build());
            current = current.plusMinutes(30);
        }
    }
}
