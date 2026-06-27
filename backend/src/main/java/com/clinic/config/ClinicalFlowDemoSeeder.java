package com.clinic.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.common.enums.AppointmentStatus;
import com.clinic.common.enums.AppointmentType;
import com.clinic.common.enums.BookingMode;
import com.clinic.common.enums.CreatedByType;
import com.clinic.common.enums.FollowUpStatus;
import com.clinic.common.enums.MedicalRecordStatus;
import com.clinic.common.enums.ServiceOrderStatus;
import com.clinic.common.enums.ServiceType;
import com.clinic.common.enums.StaffType;
import com.clinic.entity.appointment.Appointment;
import com.clinic.entity.auth.Account;
import com.clinic.entity.auth.Role;
import com.clinic.entity.crm.FollowUp;
import com.clinic.entity.crm.Notification;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.Service;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.medical.ServiceResult;
import com.clinic.entity.patient.Patient;
import com.clinic.entity.staff.Expertise;
import com.clinic.entity.staff.Staff;
import com.clinic.repository.appointment.AppointmentRepository;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.auth.RoleRepository;
import com.clinic.repository.crm.FollowUpRepository;
import com.clinic.repository.crm.NotificationRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.medical.ServiceRepository;
import com.clinic.repository.medical.ServiceResultRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.repository.staff.ExpertiseRepository;
import com.clinic.repository.staff.StaffRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seed tài khoản demo cho toàn bộ luồng khám + CLS + tái khám + thông báo.
 * Chạy một lần khi chưa có bn1@gmail.com. Mật khẩu chung: 12345678
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ClinicalFlowDemoSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "12345678";

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final ExpertiseRepository expertiseRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceResultRepository serviceResultRepository;
    private final FollowUpRepository followUpRepository;
    private final NotificationRepository notificationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Cleaning up old clinical flow demo data ===");
        cleanupOldDemoData();

        log.info("=== Seeding clinical flow demo data ===");

        Role patientRole = role("PATIENT");
        Role doctorRole = role("DOCTOR");
        Role staffRole = role("STAFF");

        Expertise expertise = expertiseRepository.findAll().stream().findFirst().orElseGet(this::createDefaultExpertise);
        Staff doctor = ensureStaff("bacsi@gmail.com", "BS. Nguyễn Minh Kiet", StaffType.DOCTOR, doctorRole, expertise);
        Staff labTech = ensureStaff("lab@gmail.com", "KTV. Trần Lab", StaffType.LAB_TECH, staffRole, null);
        ensureStaff("letan@gmail.com", "Lễ tân Mai", StaffType.STAFF, staffRole, null);

        Service labService = ensureLabService();

        LocalDate today = LocalDate.now();

        // BN1 — CHECKED_IN: sẵn sàng gọi khám (không CLS)
        Patient bn1 = createPatient("bn1@gmail.com", "BN1 - Chờ gọi khám", patientRole);
        Appointment a1 = saveAppointment(bn1, doctor, today, LocalTime.of(8, 0), AppointmentStatus.CHECKED_IN, 1, null);
        MedicalRecord r1 = saveRecord(bn1, doctor, a1, MedicalRecordStatus.IN_PROGRESS, "Chưa khám", null, null);
        notify(bn1, "Lịch khám hôm nay lúc 08:00. Bạn đã check-in, vui lòng chờ gọi tên.");

        // BN2 — IN_PROGRESS + order ORDERED: test tạo chỉ định + chuyển chờ KQ
        Patient bn2 = createPatient("bn2@gmail.com", "BN2 - Đang khám có CLS", patientRole);
        Appointment a2 = saveAppointment(bn2, doctor, today, LocalTime.of(8, 30), AppointmentStatus.IN_PROGRESS, null, null);
        MedicalRecord r2 = saveRecord(bn2, doctor, a2, MedicalRecordStatus.IN_PROGRESS, "Viêm họng nghi ngờ", "Theo dõi", null);
        saveOrder(r2, labService, doctor, ServiceOrderStatus.ORDERED);
        notify(bn2, "Đến lượt khám của bạn. Vui lòng vào phòng khám gặp Bác sĩ " + doctor.getFullName() + ".");

        // BN3 — WAITING_RESULT + order ORDERED: đang chờ Lab làm xét nghiệm
        Patient bn3 = createPatient("bn3@gmail.com", "BN3 - Chờ kết quả CLS", patientRole);
        Appointment a3 = saveAppointment(bn3, doctor, today, LocalTime.of(9, 0), AppointmentStatus.WAITING_RESULT, null, null);
        MedicalRecord r3 = saveRecord(bn3, doctor, a3, MedicalRecordStatus.WAITING_RESULT, "Nghi ngờ viêm phổi", "Chờ XN", null);
        saveOrder(r3, labService, doctor, ServiceOrderStatus.ORDERED);
        notify(bn3, "Bác sĩ đã chỉ định cận lâm sàng. Vui lòng di chuyển đến khu vực xét nghiệm/chụp chiếu.");

        // BN4 — CHECKED_IN queue=0: đã có KQ, ưu tiên đọc kết quả
        Patient bn4 = createPatient("bn4@gmail.com", "BN4 - Đọc kết quả", patientRole);
        Appointment a4 = saveAppointment(bn4, doctor, today, LocalTime.of(9, 30), AppointmentStatus.CHECKED_IN, 0, null);
        MedicalRecord r4 = saveRecord(bn4, doctor, a4, MedicalRecordStatus.WAITING_RESULT, "Theo dõi sau XN", null, null);
        ServiceOrder o4 = saveOrder(r4, labService, doctor, ServiceOrderStatus.DONE);
        saveResult(o4, labTech, "WBC: 12.000", "Viêm nhiễm nhẹ");
        notify(bn4, "Đã có kết quả xét nghiệm. Bạn đã được xếp ưu tiên vào gặp bác sĩ để đọc kết quả.");

        // BN5 — COMPLETED + follow_up PENDING
        Patient bn5 = createPatient("bn5@gmail.com", "BN5 - Đã khám + tái khám", patientRole);
        Appointment a5 = saveAppointment(bn5, doctor, today.minusDays(1), LocalTime.of(10, 0), AppointmentStatus.COMPLETED, null, LocalDateTime.now().minusDays(1));
        MedicalRecord r5 = saveRecord(bn5, doctor, a5, MedicalRecordStatus.DONE, "Viêm đường hô hấp", "Thuốc + nghỉ ngơi", "Tái khám 7 ngày");
        FollowUp fu5 = saveFollowUp(r5, bn5, doctor, LocalDateTime.now().plusDays(7).withHour(9).withMinute(0), "Tái khám đánh giá đáp ứng điều trị");
        notify(bn5, "Bác sĩ " + doctor.getFullName() + " hẹn bạn tái khám vào "
                + fu5.getScheduledDatetime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + ". Ghi chú: Tái khám đáp ứng điều trị. Vui lòng xác nhận trên ứng dụng.");

        // BN6 — follow_up ngày mai (test job nhắc D-1)
        Patient bn6 = createPatient("bn6@gmail.com", "BN6 - Nhắc tái khám mai", patientRole);
        Appointment a6 = saveAppointment(bn6, doctor, today.minusDays(3), LocalTime.of(11, 0), AppointmentStatus.COMPLETED, null, null);
        MedicalRecord r6 = saveRecord(bn6, doctor, a6, MedicalRecordStatus.DONE, "Hen suyễn", "Dùng thuốc dự phòng", null);
        saveFollowUp(r6, bn6, doctor, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), "Tái khám kiểm tra phổi");

        // BN7 — CONFIRMED follow-up + nhiều thông báo
        Patient bn7 = createPatient("bn7@gmail.com", "BN7 - Đã xác nhận tái khám", patientRole);
        Appointment a7 = saveAppointment(bn7, doctor, today.minusDays(5), LocalTime.of(14, 0), AppointmentStatus.COMPLETED, null, null);
        MedicalRecord r7 = saveRecord(bn7, doctor, a7, MedicalRecordStatus.DONE, "Dị ứng da", "Tránh allergen", null);
        FollowUp fu7 = saveFollowUp(r7, bn7, doctor, LocalDateTime.now().plusDays(14).withHour(15).withMinute(0), "Tái khám da liễu");
        fu7.setStatus(FollowUpStatus.CONFIRMED);
        fu7.setConfirmedAt(LocalDateTime.now().minusDays(1));
        followUpRepository.save(fu7);
        notify(bn7, "Lịch hẹn khám của bạn đã được xác nhận.");
        notify(bn7, "Kết quả xét nghiệm máu của bạn đã sẵn sàng. Vui lòng xem trong Hồ sơ y tế.");

        log.info("""
                
                === DEMO ACCOUNTS (password: 12345678) ===
                Staff/Doctor:
                  bacsi@gmail.com   — Bác sĩ (doctor workflow)
                  letan@gmail.com   — Lễ tân
                  lab@gmail.com     — KTV Lab
                
                Patients (luồng khám):
                  bn1@gmail.com — CHECKED_IN, chờ gọi khám
                  bn2@gmail.com — IN_PROGRESS + chỉ định ORDERED → test sendToLab
                  bn3@gmail.com — WAITING_RESULT, Lab chưa nhập KQ
                  bn4@gmail.com — CHECKED_IN queue=0, đã có KQ → Đọc kết quả
                  bn5@gmail.com — COMPLETED + follow_up PENDING + thông báo tái khám
                  bn6@gmail.com — follow_up ngày mai (job nhắc D-1)
                  bn7@gmail.com — follow_up CONFIRMED + nhiều thông báo
                """);
    }

    private void cleanupOldDemoData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        String[] queries = {
            "DELETE n FROM notification n JOIN account a ON n.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE f FROM follow_up f JOIN medical_record mr ON f.record_id = mr.record_id JOIN appointment ap ON mr.appointment_id = ap.appointment_id JOIN patient p ON ap.patient_id = p.patient_id JOIN account a ON p.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE sr FROM service_result sr JOIN service_order so ON sr.order_id = so.order_id JOIN medical_record mr ON so.record_id = mr.record_id JOIN appointment ap ON mr.appointment_id = ap.appointment_id JOIN patient p ON ap.patient_id = p.patient_id JOIN account a ON p.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE so FROM service_order so JOIN medical_record mr ON so.record_id = mr.record_id JOIN appointment ap ON mr.appointment_id = ap.appointment_id JOIN patient p ON ap.patient_id = p.patient_id JOIN account a ON p.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE mr FROM medical_record mr JOIN appointment ap ON mr.appointment_id = ap.appointment_id JOIN patient p ON ap.patient_id = p.patient_id JOIN account a ON p.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE ap FROM appointment ap JOIN patient p ON ap.patient_id = p.patient_id JOIN account a ON p.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE p FROM patient p JOIN account a ON p.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com')",
            "DELETE ss FROM staff_schedule ss JOIN staff s ON ss.staff_id = s.staff_id JOIN account a ON s.account_id = a.account_id WHERE a.email IN ('bacsi@gmail.com', 'lab@gmail.com', 'letan@gmail.com')",
            "DELETE lr FROM leave_request lr JOIN staff s ON lr.staff_id = s.staff_id JOIN account a ON s.account_id = a.account_id WHERE a.email IN ('bacsi@gmail.com', 'lab@gmail.com', 'letan@gmail.com')",
            "DELETE s FROM staff s JOIN account a ON s.account_id = a.account_id WHERE a.email IN ('bacsi@gmail.com', 'lab@gmail.com', 'letan@gmail.com')",
            "DELETE ar FROM account_role ar JOIN account a ON ar.account_id = a.account_id WHERE a.email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com', 'bacsi@gmail.com', 'lab@gmail.com', 'letan@gmail.com')",
            "DELETE FROM account WHERE email IN ('bn1@gmail.com', 'bn2@gmail.com', 'bn3@gmail.com', 'bn4@gmail.com', 'bn5@gmail.com', 'bn6@gmail.com', 'bn7@gmail.com', 'bacsi@gmail.com', 'lab@gmail.com', 'letan@gmail.com')"
        };
        for (String sql : queries) {
            jdbcTemplate.execute(sql);
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private Role role(String code) {
        return roleRepository.findByRoleCode(code)
                .orElseThrow(() -> new RuntimeException("Role not found: " + code));
    }

    private Expertise createDefaultExpertise() {
        Expertise e = new Expertise();
        e.setExpertiseName("Nội tổng quát");
        return expertiseRepository.save(e);
    }

    private Staff ensureStaff(String email, String name, StaffType type, Role role, Expertise expertise) {
        if (accountRepository.existsByEmail(email)) {
            return staffRepository.findByAccount_AccountId(
                    accountRepository.findByEmail(email).orElseThrow().getAccountId()
            ).orElseThrow();
        }
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        account.setIsActive(1);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        account.setRoles(roles);
        accountRepository.save(account);

        Staff staff = new Staff();
        staff.setAccount(account);
        staff.setFullName(name);
        staff.setStaffType(type);
        staff.setExpertise(expertise);
        staff.setGender("Nam");
        staff.setPhone("0900000000");
        staff.setIsDeleted(0);
        return staffRepository.save(staff);
    }

    private Service ensureLabService() {
        List<Service> labs = serviceRepository.findByIsDeleted(0).stream()
                .filter(s -> s.getServiceType() == ServiceType.LAB_TEST)
                .toList();
        if (!labs.isEmpty()) {
            return labs.get(0);
        }
        Service s = new Service();
        s.setServiceName("Xét nghiệm máu tổng quát");
        s.setServiceType(ServiceType.LAB_TEST);
        s.setOriginalPrice(new BigDecimal("150000"));
        s.setDiscountPrice(new BigDecimal("120000"));
        s.setIsDeleted(0);
        return serviceRepository.save(s);
    }

    private Patient createPatient(String email, String name, Role patientRole) {
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        account.setIsActive(1);
        Set<Role> roles = new HashSet<>();
        roles.add(patientRole);
        account.setRoles(roles);
        accountRepository.save(account);

        Patient patient = new Patient();
        patient.setAccount(account);
        patient.setFullName(name);
        patient.setGender("Nam");
        patient.setPhone("09" + String.format("%08d", email.hashCode() & 0xFFFF));
        patient.setDateOfBirth(LocalDate.of(1990, 1, 15));
        patient.setAddress("TP.HCM - Demo");
        patient.setIsDeleted(0);
        return patientRepository.save(patient);
    }

    private Appointment saveAppointment(Patient patient, Staff doctor, LocalDate date, LocalTime time,
            AppointmentStatus status, Integer queueNumber, LocalDateTime checkout) {
        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setMainDoctor(doctor);
        a.setExpertise(doctor.getExpertise());
        a.setAppointmentDate(date);
        a.setTimeStart(time);
        a.setTimeEnd(time.plusMinutes(30));
        a.setAppointmentType(AppointmentType.WALK_IN);
        a.setBookingMode(BookingMode.DOCTOR);
        a.setCreatedBy(CreatedByType.STAFF);
        a.setStatus(status);
        a.setQueueNumber(queueNumber);
        a.setCheckinTime(LocalDateTime.of(date, time.minusMinutes(15)));
        if (checkout != null) {
            a.setCheckoutTime(checkout);
        }
        a.setIsDeleted(0);
        return appointmentRepository.save(a);
    }

    private MedicalRecord saveRecord(Patient patient, Staff doctor, Appointment appointment,
            MedicalRecordStatus status, String diagnosis, String treatment, String note) {
        MedicalRecord r = new MedicalRecord();
        r.setPatient(patient);
        r.setMainDoctor(doctor);
        r.setAppointment(appointment);
        r.setStatus(status);
        r.setDiagnosis(diagnosis);
        r.setTreatment(treatment);
        r.setNote(note);
        r.setVitalsTaken(true);
        return medicalRecordRepository.save(r);
    }

    private ServiceOrder saveOrder(MedicalRecord record, Service service, Staff orderedBy,
            ServiceOrderStatus status) {
        ServiceOrder order = new ServiceOrder();
        order.setMedicalRecord(record);
        order.setService(service);
        order.setOrderedBy(orderedBy);
        order.setPriceAtTime(service.getDiscountPrice() != null ? service.getDiscountPrice() : service.getOriginalPrice());
        order.setStatus(status);
        return serviceOrderRepository.save(order);
    }

    private void saveResult(ServiceOrder order, Staff labTech, String data, String conclusion) {
        ServiceResult result = new ServiceResult();
        result.setServiceOrder(order);
        result.setResultData(data);
        result.setConclusion(conclusion);
        result.setEnteredBy(labTech);
        serviceResultRepository.save(result);
    }

    private FollowUp saveFollowUp(MedicalRecord record, Patient patient, Staff doctor,
            LocalDateTime scheduled, String note) {
        FollowUp fu = new FollowUp();
        fu.setMedicalRecord(record);
        fu.setPatient(patient);
        fu.setDoctor(doctor);
        fu.setScheduledDatetime(scheduled);
        fu.setNote(note);
        fu.setStatus(FollowUpStatus.PENDING);
        return followUpRepository.save(fu);
    }

    private void notify(Patient patient, String content) {
        if (patient.getAccount() == null) return;
        Notification n = new Notification();
        n.setAccount(patient.getAccount());
        n.setType(Notification.Type.SYSTEM);
        n.setContent(content);
        n.setSentAt(LocalDateTime.now());
        notificationRepository.save(n);
    }
}
